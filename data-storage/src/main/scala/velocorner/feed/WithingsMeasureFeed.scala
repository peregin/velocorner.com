package velocorner.feed

import org.slf4s.Logging
import velocorner.SecretConfig

/**
* Implementation to connect with Withings REST API
*/
object WithingsMeasureFeed {

  val baseUrl = "https://wbsapi.withings.net"
}

class WithingsMeasureFeed(maybeToken: Option[String], config: SecretConfig) extends MeasureFeed with Logging {

  val token = maybeToken.getOrElse(config.getToken("withings")) // dedicated token after authentication or application generic

  override def listMeasures: String = ???
}
