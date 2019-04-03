package controllers.auth

import java.net.{URI, URLEncoder}

import StravaController.{AccessToken, ProviderUser}
import controllers.ConnectivitySettings
import play.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.feed.StravaActivityFeed
import velocorner.model.Account

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.api.libs.ws.DefaultBodyWritables._
import velocorner.model.strava.Athlete

case class AccessTokenResponse(token: AccessToken, athlete: Option[ProviderUser])

/**
  * Created by levi on 09/12/15.
  * TODO: OauthAuthenticator
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
    val encodedScope = URLEncoder.encode("activity:read_all", "utf-8")
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
      val token = (json \ "access_token").as[String]
      logger.info(s"got token[$token] for athlete $athlete")
      AccessTokenResponse(token, Some(Account.from(athlete, token, None)))
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
