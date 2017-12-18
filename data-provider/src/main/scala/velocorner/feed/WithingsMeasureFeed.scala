package velocorner.feed

import org.slf4s.Logging
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import velocorner.SecretConfig

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
* Implementation to connect with Withings REST API
*/
object WithingsMeasureFeed {

  val baseUrl = "https://api.health.nokia.com/v2"

  def consumerKey(config: SecretConfig) = ConsumerKey(config.getToken("withings"), config.getSecret("withings"))
}

class WithingsMeasureFeed(userId: Long, token: RequestToken, val config: SecretConfig) extends HttpFeed with MeasureFeed with Logging {

  log.info(s"connecting to withings with token [${token.token}]...")

  lazy val timeout = 10 seconds

  private def signer = OAuthCalculator(WithingsMeasureFeed.consumerKey(config), token)

  override def listMeasures: String = {
    val response = ws(_
      .url(s"${WithingsMeasureFeed.baseUrl}/measure")
      .withQueryStringParameters(("action", "getmeas"), ("userid", userId.toString))
      .sign(signer)
      .get())
    Await.result(response, timeout).body
  }
}
