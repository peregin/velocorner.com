package velocorner.manual.athlete

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.MyLocalConfig
import velocorner.util.CloseableResource

object AthleteFromStravaApp extends App with LazyLogging with CloseableResource with MyLocalConfig {

  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val athlete = feed.getAthlete
    logger.info(s"athlete = $athlete")
  }
  HttpFeed.shutdown()
}
