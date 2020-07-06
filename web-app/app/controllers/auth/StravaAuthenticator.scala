package controllers.auth

import java.net.{URI, URLEncoder}

import controllers.ConnectivitySettings
import controllers.auth.StravaController.{AccessToken, ProviderUser, RefreshToken}
import org.joda.time.DateTime
import play.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.JsValue
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.feed.StravaActivityFeed
import velocorner.model.Account
import velocorner.model.strava.Athlete
import velocorner.util.Metrics

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class AccessTokenResponse(accessToken: AccessToken, expiresAt: DateTime, refreshToken: RefreshToken, athlete: Option[ProviderUser])

/**
  * Created by levi on 09/12/15.
  */
class StravaAuthenticator(connectivity: ConnectivitySettings) {

  val authorizationUrl: String = StravaActivityFeed.authorizationUrl
  val clientSecret: String = connectivity.secretConfig.getSecret("strava")
  val accessTokenUrl: String = StravaActivityFeed.accessTokenUrl
  val providerName: String = "strava"
  val clientId: String = connectivity.secretConfig.getId("strava")
  val callbackUrl: String = connectivity.secretConfig.getCallbackUrl("strava")

  private val logger = Logger.of(this.getClass)

  def getAuthorizationUrl(host: String, state: String): String = {
    logger.info(s"authorization url for scope[$host]")
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    // scope is the host name localhost:9000 or www.velocorner.com
    val uri = new URI(callbackUrl)
    val adjustedCallbackUrl = s"${uri.getScheme}://$host${uri.getPath}"
    val encodedRedirectUri = URLEncoder.encode(adjustedCallbackUrl, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    val encodedScope = URLEncoder.encode("read,activity:read", "utf-8")
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=auto&scope=$encodedScope"
  }

  def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessTokenResponse] = {
     logger.info(s"retrieve token for code[$code]")
     val feed = connectivity.getStravaFeed
     val resp = feed.ws(_.url(accessTokenUrl))
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Map(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      )
      .map(parseAccessTokenResponse)
     resp.onComplete(_ => feed.close())
     resp
  }

  def parseAccessTokenResponse(response: StandaloneWSResponse): AccessTokenResponse = {
    logger.info(s"parsing token from $response")
    try {
      val json = response.body[JsValue]
      val athlete = (json \ "athlete").as[Athlete]
      // access token, expires usually in 6 hours
      val accessToken = (json \ "access_token").as[String]
      // refresh token allows to retrieve a new access token when the current has been expired
      val refreshToken = (json \ "refresh_token").as[String]
      // access token expires in seconds
      val expiresInSec = (json \ "expires_in").as[Int]
      logger.info(s"accessToken valid for ${Metrics.elapsedTimeText(expiresInSec * 1000)}")
      val expiresAt = DateTime.now().plusSeconds(expiresInSec)
      logger.info(s"got token[$accessToken] until $expiresAt for athlete $athlete")
      AccessTokenResponse(accessToken, expiresAt, refreshToken, Some(Account.from(athlete, accessToken, expiresAt, refreshToken)))
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
