package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed


object StatisticsApp extends App with Logging with MyMacConfig {

  val feed = new StravaActivityFeed(None, SecretConfig.load())
  val statistics = feed.getStatistics(432909)
  feed.wsClient.close()
  log.info(s"stats $statistics")
}
