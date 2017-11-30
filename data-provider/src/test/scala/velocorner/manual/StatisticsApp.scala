package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.util.CloseableResource


object StatisticsApp extends App with Logging with CloseableResource with MyMacConfig {

  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val statistics = feed.getStatistics(432909)
    log.info(s"stats $statistics")
  }
  HttpFeed.shutdown()
}
