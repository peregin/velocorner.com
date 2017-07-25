package controllers

import javax.inject.Inject

import controllers.auth.{AuthConfigSupport, StravaAuthenticator, StravaProviderUserSupport}
import jp.t2v.lab.play2.auth.social.core.{OAuth2Authenticator, OAuth2Controller}
import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import play.api.Logger
import play.api.mvc.{Action, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global


class AuthController @Inject()(val connectivity: ConnectivitySettings) extends AuthConfigSupport with OAuth2Controller
  with OptionalAuthElement with LoginLogout
  with StravaProviderUserSupport {

  override protected val authenticator: OAuth2Authenticator = new StravaAuthenticator(connectivity)

  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth link succeeded with token[$token]")
    retrieveProviderUser(token).map{providerUser =>
      connectivity.storage.store(providerUser)
      Redirect(routes.ApplicationController.index)
    }
  }

  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth login succeeded with token[$token]")
    retrieveProviderUser(token).flatMap { providerUser =>
      val storage = connectivity.storage
      val maybeAccount = storage.getAccount(providerUser.athleteId)
      Logger.info(s"account for token[$token] is $maybeAccount")
      if (maybeAccount.isEmpty) storage.store(providerUser)
      gotoLoginSucceeded(providerUser.athleteId)
    }
  }

  def logout = Action.async{ implicit request =>
    gotoLogoutSucceeded
  }
}
