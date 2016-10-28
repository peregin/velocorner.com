package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.WithingsMeasureFeed

/**
  * Created by levi on 23.10.16.
  */
object MeasuresFromWithingsApp extends App with Logging with MyMacConfig {

  val config = SecretConfig.load()
  val feed = new WithingsMeasureFeed(None, config)

  val res = feed.listMeasures
  log.info(s"result is $res")
}
