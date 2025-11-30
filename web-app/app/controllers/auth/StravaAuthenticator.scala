package controllers.auth

import java.net.{URI, URLEncoder}

import controllers.ConnectivitySettings
import org.joda.time.DateTime
import play.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.JsValue
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.ServiceProvider
import velocorner.feed.OAuth2._
import velocorner.feed.StravaActivityFeed
import velocorner.model.strava.Athlete
import velocorner.util.Metrics

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

// for kestrel combinator and unsafeTap
import mouse.all._

/**
 * Created by levi on 09/12/15.
 */
class StravaAuthenticator(connectivity: ConnectivitySettings) {

  private val clientSecret: String = connectivity.secretConfig.getAuthSecret(ServiceProvider.Strava)
  private val accessTokenUrl: String = StravaActivityFeed.accessTokenUrl
  private val clientId: String = connectivity.secretConfig.getAuthId(ServiceProvider.Strava)

  private val logger = Logger.of(this.getClass)

  def getAuthorizationUrl(host: String, state: String): String = {
    logger.info(s"authorization url for host[$host]")
    val authorizationUrl: String = StravaActivityFeed.authorizationUrl
    val callbackUri = new URI(connectivity.secretConfig.getAuthCallbackUrl(ServiceProvider.Strava))
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    // the host is mainly localhost:9001 or www.velocorner.com
    val adjustedCallbackUrl = s"${callbackUri.getScheme}://$host${callbackUri.getPath}"
    val encodedRedirectUri = URLEncoder.encode(adjustedCallbackUrl, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    val encodedScope = URLEncoder.encode("read,activity:read,profile:read_all", "utf-8")
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=auto&scope=$encodedScope"
  }

  def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[OAuth2TokenResponse] = {
    logger.info(s"retrieve token for code[$code]")
    val feed = connectivity.getStravaFeed
    val resp = feed
      .ws(_.url(accessTokenUrl))
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Map("client_id" -> clientId, "client_secret" -> clientSecret, "code" -> code))
      .map(parseAccessTokenResponse)
    resp <| (_.onComplete(_ => feed.close()))
  }

  // invoked when accessing Strava API but the current token is expired
  def refreshAccessToken(refreshToken: RefreshToken)(implicit ctx: ExecutionContext): Future[OAuth2TokenResponse] = {
    logger.info(s"refreshing token with[$refreshToken]")
    val feed = connectivity.getStravaFeed
    val resp = feed
      .ws(_.url(accessTokenUrl))
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(
        Map(
          "client_id" -> clientId,
          "client_secret" -> clientSecret,
          "grant_type" -> "refresh_token",
          "refresh_token" -> refreshToken
        )
      )
      .map(parseAccessTokenResponse)
    resp <| (_.onComplete(_ => feed.close()))
  }

  def parseAccessTokenResponse(response: StandaloneWSResponse): OAuth2TokenResponse = {
    logger.info(s"parsing token from $response")
    try {
      val json = response.body[JsValue]
      val athlete = (json \ "athlete").asOpt[Athlete]
      // access token, expires usually in 6 hours
      val accessToken = (json \ "access_token").as[String]
      // refresh token allows to retrieve a new access token when the current has been expired
      val refreshToken = (json \ "refresh_token").as[String]
      // access token expires in seconds
      val expiresInSec = (json \ "expires_in").as[Int]
      logger.info(s"accessToken valid for ${Metrics.elapsedTimeText(expiresInSec * 1000)}")
      val expiresAt = DateTime.now().plusSeconds(expiresInSec)
      logger.info(s"got token[$accessToken] until $expiresAt for athlete $athlete")
      OAuth2TokenResponse(accessToken, expiresAt, refreshToken, athlete)
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
