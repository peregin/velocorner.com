package velocorner.manual

import org.slf4s.Logging
import play.api.libs.oauth
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, WithingsMeasureFeed}
import velocorner.util.CloseableResource

/**
  * Created by levi on 23.10.16.
  */
object MeasuresFromWithingsApp extends App with Logging with CloseableResource with MyMacConfig {

  val config = SecretConfig.load()
  withCloseable(new WithingsMeasureFeed(3112606, oauth.RequestToken(args(0), args(1)), config)) { feed =>
    val res = feed.listMeasures
    log.info(s"result is $res")
  }
  HttpFeed.shutdown()
}
