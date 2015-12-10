package controllers.auth

import java.net.URLEncoder

import controllers.Global
import jp.t2v.lab.play2.auth.social.core.{AccessTokenRetrievalFailedException, OAuth2Authenticator}
import org.slf4s.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc.Results
import velocorner.proxy.StravaFeed

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.api.Play.current

/**
  * Created by levi on 09/12/15.
  */
class StravaAuthenticator extends OAuth2Authenticator with Logging {

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

  override def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken] = {
    log.info(s"retrieve token for code[$code]")
    WS.url(accessTokenUrl)
      .withQueryString(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Results.EmptyContent())
      .map { response =>
        log.debug("retrieving access token from provider API: " + response.body)
        parseAccessTokenResponse(response)
      }
  }

  override def parseAccessTokenResponse(response: WSResponse): String = {
    log.info("parsing token")
    try {
      (response.json \ "access_token").as[String]
    } catch {
      case NonFatal(e) =>
        throw new AccessTokenRetrievalFailedException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
