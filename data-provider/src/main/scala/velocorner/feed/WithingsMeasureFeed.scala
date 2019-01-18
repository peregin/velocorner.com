package velocorner.feed

import org.slf4s.Logging
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.shaded.ahc.org.asynchttpclient.{Request, RequestBuilderBase}
import velocorner.SecretConfig

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import collection.JavaConverters._

/**
* Implementation to connect with Withings REST API
*/
object WithingsMeasureFeed {

  // for getting body measures
  val baseUrl = "https://api.health.nokia.com"

  def consumerKey(config: SecretConfig) = ConsumerKey(config.getToken("withings"), config.getSecret("withings"))
}

class WithingsMeasureFeed(userId: Long, token: RequestToken, val config: SecretConfig) extends HttpFeed with MeasureFeed with Logging {

  log.info(s"connecting to withings with token [${token.token}]...")

  private def signer = new OAuthCalculator(WithingsMeasureFeed.consumerKey(config), token) {
    override def calculateAndAddSignature(request: Request, requestBuilder: RequestBuilderBase[_]): Unit = {
      super.calculateAndAddSignature(request, requestBuilder)
      val maybeAuthHeader = request.getHeaders.asScala.toSeq.filter(m => m.getKey.compareToIgnoreCase("Authorization") == 0).headOption
      maybeAuthHeader.foreach{h =>
        val auth = h.getValue
        log.info(s"auth header: $auth")
        val pairs = auth.stripPrefix("OAuth").split(',').map(_.trim)
        pairs.foreach{p =>
          val keyValue = p.split('=')
          if (keyValue.length > 1) {
            val paramName = keyValue(0).trim
            val paramValue = keyValue(1).trim.stripPrefix("\"").stripSuffix("\"")
            log.info(s"$paramName=$paramValue")
            requestBuilder.addQueryParam(paramName, paramValue)
          }
        }
      }
    }
  }

  override def listMeasures: String = {
    val response = ws{_.url(s"${WithingsMeasureFeed.baseUrl}/measure")
      .withQueryStringParameters(
        ("action", "getmeas"),
        ("userid", userId.toString)
      )
      .sign(signer)
      .get()
    }
    Await.result(response, timeout).body
  }
}
