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
    logger.info("exchanging Strava authorization code")
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
    logger.info("refreshing Strava access token")
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
    logger.info(s"parsing Strava token response with status ${response.status}")
    try {
      val json = response.body[JsValue]
      val athlete = (json \ "athlete").asOpt[Athlete]
      // access token, expires usually in 6 hours
      val accessToken = (json \ "access_token").as[String]
      // refresh token allows to retrieve a new access token when the current has been expired
      val refreshToken = (json \ "refresh_token").as[String]
      // access token expires in seconds
      val expiresInSec = (json \ "expires_in").as[Int]
      val expiresAt = DateTime.now().plusSeconds(expiresInSec)
      logger.info(s"received Strava token valid until $expiresAt for athlete ${athlete.map(_.id)}")
      OAuth2TokenResponse(accessToken, expiresAt, refreshToken, athlete)
    } catch {
      case NonFatal(e) =>
        // Preserve the refresh callers' invalid-code marker without leaking parser messages, causes, or response data.
        val reason =
          if (response.body.toLowerCase.contains("\"code\":\"invalid\"")) """{"code":"invalid"}"""
          else e.getClass.getSimpleName
        throw new IllegalArgumentException(s"Failed to parse Strava token response (status ${response.status}, $reason)")
    }
  }
}
