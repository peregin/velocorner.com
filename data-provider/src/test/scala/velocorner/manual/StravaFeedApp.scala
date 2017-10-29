package velocorner.manual

import java.util.concurrent.Executors

import org.slf4s.Logging
import play.api.libs.ws.StandaloneWSRequest
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed

import scala.concurrent.{ExecutionContext, Future}

object StravaFeedApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new StravaActivityFeed(None, config)
  private val executorService = Executors.newFixedThreadPool(5)
  implicit val executionContext = ExecutionContext.fromExecutor(executorService)


  log.info("connecting...")
  val response: Future[StandaloneWSRequest#Response] = feed.ws(_.url("https://strava.com")).get()
  log.info("retrieving...")
  response.onSuccess{
    case reply =>
      log.info(reply.body)
      log.info(reply.statusText)
  }

  response.onComplete{
    case _ =>
      feed.close()
      executorService.shutdownNow()
  }
}
