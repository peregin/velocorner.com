package velocorner.manual.athlete

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.manual.MyMacConfig

object AthleteFromStravaApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new StravaActivityFeed(None, config)
  val athlete = feed.getAthlete
  log.info(s"athlete = $athlete")
  feed.close()
}
