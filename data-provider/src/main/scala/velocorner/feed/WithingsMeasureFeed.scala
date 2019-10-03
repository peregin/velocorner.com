package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.shaded.ahc.org.asynchttpclient.{Request, RequestBuilderBase}
import velocorner.SecretConfig

import scala.concurrent.Await
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

/**
* Implementation to connect with Withings REST API
*/
object WithingsMeasureFeed {

  // for getting body measures
  val baseUrl = "https://api.health.nokia.com"

  def consumerKey(config: SecretConfig) = ConsumerKey(config.getToken("withings"), config.getSecret("withings"))
}

class WithingsMeasureFeed(userId: Long, token: RequestToken, val config: SecretConfig) extends HttpFeed with MeasureFeed with LazyLogging {

  logger.info(s"connecting to withings with token [${token.token}]...")

  private def signer = new OAuthCalculator(WithingsMeasureFeed.consumerKey(config), token) {
    override def calculateAndAddSignature(request: Request, requestBuilder: RequestBuilderBase[_]): Unit = {
      super.calculateAndAddSignature(request, requestBuilder)
      val maybeAuthHeader = request.getHeaders.asScala.toSeq.find(m => m.getKey.compareToIgnoreCase("Authorization") == 0)
      maybeAuthHeader.foreach{h =>
        val auth = h.getValue
        logger.info(s"auth header: $auth")
        val pairs = auth.stripPrefix("OAuth").split(',').map(_.trim)
        pairs.foreach{p =>
          val keyValue = p.split('=')
          if (keyValue.length > 1) {
            val paramName = keyValue(0).trim
            val paramValue = keyValue(1).trim.stripPrefix("\"").stripSuffix("\"")
            logger.info(s"$paramName=$paramValue")
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
