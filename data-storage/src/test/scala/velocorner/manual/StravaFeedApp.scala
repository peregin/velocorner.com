package velocorner.manual

import org.slf4s.Logging
import play.api.libs.ws.{WS, WSResponse}
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed

import scala.concurrent.Future

object StravaFeedApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new StravaFeed(None, config)
  implicit val executionContext = feed.executionContext

  log.info("connecting...")
  val response: Future[WSResponse] = WS.clientUrl("https://strava.com")(feed.wsClient).get()
  log.info("retrieving...")
  response.onSuccess{
    case reply =>
      log.info(reply.body.length.toString)
      log.info(reply.statusText)
  }
  response.onComplete(_ => feed.wsClient.close())
}
