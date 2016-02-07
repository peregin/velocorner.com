package controllers

import controllers.auth.{AuthConfigSupport, StravaAuthenticator, StravaProviderUserSupport}
import jp.t2v.lab.play2.auth.social.core.{OAuth2Authenticator, OAuth2Controller}
import jp.t2v.lab.play2.auth.{Login, OptionalAuthElement}
import play.api.Logger
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}


object AuthController extends AuthConfigSupport with OAuth2Controller
  with OptionalAuthElement with Login
  with StravaProviderUserSupport {

  override protected val authenticator: OAuth2Authenticator = new StravaAuthenticator

  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth link succeeded with token[$token]")
    retrieveProviderUser(token).map{providerUser =>
      Global.getStorage.store(providerUser)
      Redirect(routes.Application.index)
    }
  }

  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth login succeeded with token[$token]")
    retrieveProviderUser(token).flatMap { providerUser =>
      val storage = Global.getStorage
      val maybeAccount = storage.getAccount(providerUser.id)
      if (maybeAccount.isEmpty) storage.store(providerUser)
      gotoLoginSucceeded(providerUser.id)
    }
  }
}
