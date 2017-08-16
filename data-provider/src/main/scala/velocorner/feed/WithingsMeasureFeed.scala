package velocorner.feed

import org.slf4s.Logging
import velocorner.SecretConfig

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
* Implementation to connect with Withings REST API
*/
object WithingsMeasureFeed {

  val baseUrl = "https://api.health.nokia.com/v2"
}

class WithingsMeasureFeed(maybeToken: Option[String], val config: SecretConfig) extends HttpFeed with MeasureFeed with Logging {

  val token = maybeToken.getOrElse(config.getToken("withings")) // dedicated token after authentication or application generic
  log.info(s"connecting to withings with token [$token]...")

  val authHeader = s"Bearer $token"
  val timeout = 10 seconds

  // TODO: setup
  // oauth_consumer_key =
  // oauth_nonce = AAA
  // oauth_timestamp = 1477376000
  // oauth_token = AAA
  // oauth_signature = BBB
  override def listMeasures: String = {
    val response = ws(_.url(s"${WithingsMeasureFeed.baseUrl}/measure?action=getmeas").withHttpHeaders(("Authorization", authHeader)).get())
    Await.result(response, timeout).body
  }
}
