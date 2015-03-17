package velocorner.manual

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import play.api.libs.ws.{DefaultWSClientConfig, WSResponse, WS}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by levi on 18/03/15.
 */
object HttpApp extends App {

  val config = new NingAsyncHttpClientConfigBuilder(DefaultWSClientConfig()).build()
  val builder = new AsyncHttpClientConfig.Builder(config)
  implicit val wsClient = new NingWSClient(builder.build())
  implicit val executionContext = ExecutionContext.Implicits.global

  println("connecting...")
  val response: Future[WSResponse] = WS.clientUrl("http://velocorner.com").get()
  println("retrieving...")
  response.onSuccess{
    case reply =>
      println(reply.body.size)
      println(reply.statusText)
  }
}
