package controllers

import jp.t2v.lab.play2.auth.{Login, OptionalAuthElement, AuthConfig}
import jp.t2v.lab.play2.auth.social.core.{OAuth2Authenticator, OAuthProviderUserSupport, OAuth2Controller}
import play.api.libs.ws.WSResponse
import play.api.mvc.{Result, RequestHeader}
import velocorner.model.Account
import velocorner.proxy.StravaFeed

import scala.concurrent.{Future, ExecutionContext}


/**
 * Created by levi on 07/12/15.
 */
trait StravaAuthController extends OAuth2Controller with AuthConfig with OptionalAuthElement with Login {

  // val authenticator
  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }

  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }
}

object StravaAuthController extends StravaAuthController with AuthConfigSupport with OAuthProviderUserSupport {

  override type ProviderUser = Account

  override protected val authenticator: OAuth2Authenticator = new OAuth2Authenticator {

    override type AccessToken = String

    override val authorizationUrl: String = StravaFeed.authorizationUrl
    override val clientSecret: String = Global.getSecretConfig.getApplicationSecret
    override val accessTokenUrl: String = StravaFeed.accessTokenUrl
    override val providerName: String = "strava"
    override val clientId: String = Global.getSecretConfig.getApplicationId
    override val callbackUrl: String = "mycallbackUrl"

    override def getAuthorizationUrl(scope: String, state: String): String = ???

    override def parseAccessTokenResponse(response: WSResponse): String = ???

    override def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken] = ???
  }

  override def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    ???
  }
}
