package controllers

import controllers.auth.{StravaAuthenticator, StravaProviderUserSupport}
import jp.t2v.lab.play2.auth.social.core.{OAuth2Authenticator, OAuth2Controller}
import jp.t2v.lab.play2.auth.{Login, OptionalAuthElement}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}


object StravaAuthController extends AuthConfigSupport with OAuth2Controller
  with OptionalAuthElement with Login
  with StravaProviderUserSupport
  with PlayLogging {

  override protected val authenticator: OAuth2Authenticator = new StravaAuthenticator

  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    log.info(s"oauth link succeeded with token[$token]")
    retrieveProviderUser(token).map{providerUser =>
      // TODO: save into the storage
      Redirect(routes.Application.index)
    }
  }

  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    log.info(s"oauth login succeeded with token[$token]")
    retrieveProviderUser(token).flatMap { providerUser =>
      // TODO: find user // create or pass it
      gotoLoginSucceeded(0L)
    }
  }
}
