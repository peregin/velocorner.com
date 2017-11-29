package controllers.auth

import java.net.{URI, URLEncoder}

import AuthController.{AccessToken, ProviderUser}
import controllers.ConnectivitySettings
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.feed.StravaActivityFeed
import velocorner.model.{Account, Athlete}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import play.api.libs.ws.DefaultBodyWritables._

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

   def getAuthorizationUrl(scope: String, state: String): String = {
    Logger.info(s"authorization url for scope[$scope]")
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    // scope is the host name localhost:9000 or www.velocorner.com
    val uri = new URI(callbackUrl)
    val adjustedCallbackUrl = s"${uri.getScheme}://$scope${uri.getPath}"
    val encodedRedirectUri = URLEncoder.encode(adjustedCallbackUrl, "utf-8")
    //val encodedScope = URLEncoder.encode(scope, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=auto&scope=public"
  }

   def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessTokenResponse] = {
    Logger.info(s"retrieve token for code[$code]")
    connectivity.getFeed.ws(_.url(accessTokenUrl))
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Map(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      )
      .map(parseAccessTokenResponse)
  }

  def parseAccessTokenResponse(response: StandaloneWSResponse): AccessTokenResponse = {
    Logger.info(s"parsing token from $response")
    try {
      val json = response.body[JsValue]
      val athlete = (json \ "athlete").as[Athlete]
      val token = (json \ "access_token").as[String]
      Logger.info(s"got token[$token] for athlete $athlete")
      AccessTokenResponse(token, Some(Account.from(athlete, token, None)))
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
