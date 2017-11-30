package velocorner.manual.athlete

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.MyMacConfig
import velocorner.util.CloseableResource

object AthleteFromStravaApp extends App with Logging with CloseableResource with MyMacConfig {

  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val athlete = feed.getAthlete
    log.info(s"athlete = $athlete")
  }
  HttpFeed.shutdown()
}
