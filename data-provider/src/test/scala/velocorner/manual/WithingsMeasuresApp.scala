package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.oauth
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, WithingsMeasureFeed}
import velocorner.util.CloseableResource

/**
  * Created by levi on 23.10.16.
  */
object WithingsMeasuresApp extends App with LazyLogging with CloseableResource with MyMacConfig {

  val config = SecretConfig.load()
  withCloseable(new WithingsMeasureFeed(3112606, oauth.RequestToken(args(0), args(1)), config)) { feed =>
    val res = feed.listMeasures
    logger.info(s"result is $res")
  }
  HttpFeed.shutdown()
}
