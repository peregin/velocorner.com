package velocorner.feed

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.shaded.ahc.org.asynchttpclient.proxy.ProxyServer
import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig, Realm}
import velocorner.SecretConfig

import scala.collection.JavaConverters._

/**
  * Created by levi on 25.10.16.
  */
trait HttpFeed {

  val config: SecretConfig

  val httpConfigBuilder = new DefaultAsyncHttpClientConfig.Builder()

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

  implicit val system = ActorSystem.create("ws-feed")
  implicit val materializer = ActorMaterializer()

  private val asyncHttpClient = new DefaultAsyncHttpClient(httpConfigBuilder.build())
  private val wsClient = new StandaloneAhcWSClient(asyncHttpClient)

  def ws[T](func: StandaloneWSClient => T): T = {
    val client = wsClient
    try {
      func(client)
    } finally {
      //client.close()
    }
  }
}
