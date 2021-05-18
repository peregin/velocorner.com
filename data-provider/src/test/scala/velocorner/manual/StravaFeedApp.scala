package velocorner.manual

import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.StandaloneWSRequest
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object StravaFeedApp extends App with LazyLogging with MyLocalConfig {

  val config = SecretConfig.load()
  val feed = new StravaActivityFeed(None, config)
  private val executorService = Executors.newFixedThreadPool(5)
  implicit private val executionContext = ExecutionContext.fromExecutor(executorService)

  logger.info("connecting...")
  val response: Future[StandaloneWSRequest#Response] = feed.ws(_.url("https://strava.com")).get()
  logger.info("retrieving...")

  response.andThen { case _ =>
    feed.close()
    HttpFeed.shutdown()
    executorService.shutdownNow()
  }

  response.onComplete {
    case Success(reply) =>
      logger.info(reply.body)
      logger.info(reply.statusText)
    case Failure(any) =>
      logger.error("failed to query", any)
  }
}
