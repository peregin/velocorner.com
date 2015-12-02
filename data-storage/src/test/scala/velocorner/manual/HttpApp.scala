package velocorner.manual

import com.ning.http.client.AsyncHttpClientConfig
import org.slf4s.Logging
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by levi on 18/03/15.
 */
object HttpApp extends App with Logging {

  val config = new NingAsyncHttpClientConfigBuilder().build()
  val builder = new AsyncHttpClientConfig.Builder(config)
  implicit val wsClient = new NingWSClient(builder.build())
  implicit val executionContext = ExecutionContext.Implicits.global

  log.info("connecting...")
  val response: Future[WSResponse] = WS.clientUrl("http://velocorner.com").get()
  log.info("retrieving...")
  response.onSuccess{
    case reply =>
      log.info(reply.body.length.toString)
      log.info(reply.statusText)
  }
  response.onComplete(_ => wsClient.close())
}
