package controllers.auth

import java.net.{URI, URLEncoder}

import controllers.ConnectivitySettings
import jp.t2v.lab.play2.auth.social.core.AccessTokenRetrievalFailedException
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.feed.StravaActivityFeed

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal


/**
  * Created by levi on 09/12/15.
  */
class StravaAuthenticator(connectivity: ConnectivitySettings) {//extends OAuth2Authenticator {

  type AccessToken = String

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
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=force&scope=public"
  }

   def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken] = {
    Logger.info(s"retrieve token for code[$code]")
    connectivity.getFeed.ws(_.url(accessTokenUrl))
      .withQueryStringParameters(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .get()
      .map(parseAccessTokenResponse)
  }

  def parseAccessTokenResponse(response: StandaloneWSResponse): String = {
    Logger.info("parsing token")
    try {
      val json = response.body[JsValue]
      val athleteId = (json \ "athlete" \ "id").as[Int]
      Logger.info(s"token for athlete $athleteId")
      (json \ "access_token").as[String]
    } catch {
      case NonFatal(e) => throw new AccessTokenRetrievalFailedException(s"Failed to parse access token: ${response.body}", e)
    }
  }

  //override def parseAccessTokenResponse(response: WSResponse) = ???
}
