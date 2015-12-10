package controllers.auth

import java.net.URLEncoder

import controllers.Global
import jp.t2v.lab.play2.auth.social.core.OAuth2Authenticator
import play.api.libs.ws.WSResponse
import velocorner.proxy.StravaFeed

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by levi on 09/12/15.
  */
class StravaAuthenticator extends OAuth2Authenticator {

  override type AccessToken = String

  override val authorizationUrl: String = StravaFeed.authorizationUrl
  override val clientSecret: String = Global.getSecretConfig.getApplicationSecret
  override val accessTokenUrl: String = StravaFeed.accessTokenUrl
  override val providerName: String = "strava"
  override val clientId: String = Global.getSecretConfig.getApplicationId
  override val callbackUrl: String = Global.getSecretConfig.getApplicationCallbackUrl

  override def getAuthorizationUrl(scope: String, state: String): String = {
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    val encodedRedirectUri = URLEncoder.encode(callbackUrl, "utf-8")
    val encodedScope = URLEncoder.encode(scope, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=force&scope=public"
  }

  override def parseAccessTokenResponse(response: WSResponse): String = ???

  override def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken] = ???
}
