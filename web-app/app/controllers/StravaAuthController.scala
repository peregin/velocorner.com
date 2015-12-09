package controllers

import controllers.auth.{StravaController, StravaProviderUserSupport}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}


object StravaAuthController extends StravaController with AuthConfigSupport with StravaProviderUserSupport {


  // val authenticator
  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }

  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }
}
