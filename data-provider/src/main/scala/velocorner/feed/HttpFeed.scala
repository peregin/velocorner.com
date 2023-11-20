package velocorner.feed

import java.io.Closeable
import java.util.concurrent.Executors
import org.apache.pekko.actor.ActorSystem
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

  implicit private val system: ActorSystem = ActorSystem.create("ws-feed")
  private val processors = sys.runtime.availableProcessors()
  logger.info(s"available processors $processors")
  implicit private val executors: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(processors.min(5)))

  def shutdown(): Future[Unit] =
    system.terminate().map(_ => ())
}

/**
 * Created by levi on 25.10.16.
 */
trait HttpFeed extends Closeable {

  import HttpFeed._

  val config: SecretConfig

  lazy implicit val executors: ExecutionContextExecutor = HttpFeed.executors
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
  private val wsClient = new StandaloneAhcWSClient(asyncHttpClient)

  def ws[T](func: StandaloneWSClient => T): T = {
    val client = wsClient
    try
      func(client)
    finally {
      // cleanup any resources here
    }
  }

  def close(): Unit = wsClient.close()
}
