package velocorner.feed

import java.io.Closeable
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.shaded.ahc.org.asynchttpclient.proxy.{ProxyServer, ProxyType}
import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig, Realm}
import velocorner.SecretConfig

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object HttpFeed extends LazyLogging {

  private lazy val system: ActorSystem = ActorSystem.create("ws-feed")
  lazy val materializer: Materializer = Materializer(system)

  def shutdown(): Future[Unit] =
    system.terminate().map(_ => ())(using ExecutionContext.parasitic)
}

/**
 * Created by levi on 25.10.16.
 */
trait HttpFeed extends Closeable {

  val config: SecretConfig

  def materializer: Materializer = HttpFeed.materializer
  lazy implicit val executors: ExecutionContextExecutor = materializer.executionContext
  lazy protected val timeout: FiniteDuration = 10 seconds
  private val httpConfigBuilder = new DefaultAsyncHttpClientConfig.Builder()

  // setup secure proxy if it is configured w/o authentication
  for (proxyHost <- config.getProxyHost; proxyPort <- config.getProxyPort) {
    val proxyServer = (config.getProxyUser, config.getProxyPassword) match {
      case (Some(proxyUser), Some(proxyPassword)) =>
        val realm = new Realm.Builder(proxyUser, proxyPassword).build()
        new ProxyServer(proxyHost, proxyPort, 443, realm, List.empty[String].asJava, ProxyType.HTTP)
      case _ =>
        new ProxyServer(proxyHost, proxyPort, 443, null, List.empty[String].asJava, ProxyType.HTTP)
    }
    httpConfigBuilder.setProxyServer(proxyServer)
  }

  private val asyncHttpClient = new DefaultAsyncHttpClient(httpConfigBuilder.build())
  private val wsClient = new StandaloneAhcWSClient(asyncHttpClient)(using materializer)

  def ws[T](func: StandaloneWSClient => T): T = func(wsClient)

  def close(): Unit = wsClient.close()
}
