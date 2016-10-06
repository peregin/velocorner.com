package velocorner.manual

import org.slf4s.Logging
import play.api.libs.ws.WSResponse
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed

import scala.concurrent.{ExecutionContext, Future}

object StravaFeedApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new StravaFeed(None, config)
  implicit val executionContext = ExecutionContext.global


  log.info("connecting...")
  val response: Future[WSResponse] = feed.wsClient.url("https://strava.com").get()
  log.info("retrieving...")
  response.onSuccess{
    case reply =>
      log.info(reply.body.length.toString)
      log.info(reply.statusText)
  }
  response.onComplete(_ => feed.wsClient.close())
}
