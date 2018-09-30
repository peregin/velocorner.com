package velocorner.manual

import java.util.concurrent.Executors

import org.slf4s.Logging
import play.api.libs.ws.StandaloneWSRequest
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object StravaFeedApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new StravaActivityFeed(None, config)
  private val executorService = Executors.newFixedThreadPool(5)
  implicit private val executionContext = ExecutionContext.fromExecutor(executorService)


  log.info("connecting...")
  val response: Future[StandaloneWSRequest#Response] = feed.ws(_.url("https://strava.com")).get()
  log.info("retrieving...")

  response.andThen{ case _ =>
    feed.close()
    HttpFeed.shutdown()
    executorService.shutdownNow()
  }

  response.onComplete{
    case Success(reply) =>
      log.info(reply.body)
      log.info(reply.statusText)
    case Failure(any) =>
      log.error("failed to query", any)
  }
}
