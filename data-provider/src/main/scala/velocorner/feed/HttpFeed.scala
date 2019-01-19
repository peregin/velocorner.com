package velocorner.feed

import java.io.Closeable
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.shaded.ahc.org.asynchttpclient.proxy.ProxyServer
import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig, Realm}
import velocorner.SecretConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import org.slf4s.Logging

import scala.concurrent.ExecutionContext

object HttpFeed extends Logging {

  implicit val system = ActorSystem.create("ws-feed")
  implicit val materializer = ActorMaterializer()
  val processors = sys.runtime.availableProcessors()
  //log.info(s"available processors $processors")
  implicit val executors = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(processors.min(5)))

  def shutdown() {
    materializer.shutdown()
    system.terminate()
  }
}

/**
  * Created by levi on 25.10.16.
  */
trait HttpFeed extends Closeable {

  import HttpFeed._

  val config: SecretConfig

  val httpConfigBuilder = new DefaultAsyncHttpClientConfig.Builder()

  lazy val timeout = 10 seconds
  lazy implicit val executors = HttpFeed.executors


  // setup secure proxy if it is configured w/o authentication
  for (proxyHost <- config.getProxyHost; proxyPort <- config.getProxyPort) {
    val proxyServer = (config.getProxyUser, config.getProxyPassword) match {
      case (Some(proxyUser), Some(proxyPassword)) =>
        val realm = new Realm.Builder(proxyUser, proxyPassword).build()
        new ProxyServer(proxyHost, proxyPort, 443, realm, List.empty[String].asJava)
      case _ =>
        new ProxyServer(proxyHost, proxyPort, 443, null, List.empty[String].asJava)
    }
    httpConfigBuilder.setProxyServer(proxyServer)
  }

  private val asyncHttpClient = new DefaultAsyncHttpClient(httpConfigBuilder.build())
  private val wsClient = new StandaloneAhcWSClient(asyncHttpClient)

  def ws[T](func: StandaloneWSClient => T): T = {
    val client = wsClient
    try {
      func(client)
    } finally {
      // cleanup any resources here
    }
  }

  def close() = {
    wsClient.close()
  }
}
