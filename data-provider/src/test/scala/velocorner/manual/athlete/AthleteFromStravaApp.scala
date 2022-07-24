package velocorner.manual.athlete

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.util.CloseableResource

object AthleteFromStravaApp extends App with LazyLogging with CloseableResource with AwaitSupport with MyLocalConfig {

  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val athlete = awaitOn(feed.getAthlete)
    logger.info(s"athlete = $athlete")
  }
  HttpFeed.shutdown()
}
