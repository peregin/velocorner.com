package controllers

import java.util.UUID
import javax.inject.Inject

import controllers.auth.{AuthConfigSupport, StravaAuthenticator}
import jp.t2v.lab.play2.auth.social.core.OAuth2Authenticator
import play.api.mvc.Results._
import play.api.mvc._
import velocorner.model.Account


object Oauth2Controller {

  def loggedIn(request: Request[AnyContent]): Option[Account] = {
    // TODO:
    //request.attrs.get(Account)
    None
  }
}

class OAuth2Controller @Inject()(val connectivity: ConnectivitySettings) extends AuthConfigSupport {

  protected val authenticator: OAuth2Authenticator = new StravaAuthenticator(connectivity)

  protected val OAuth2StateKey = "velocorner.oauth2.state"

  def login(scope: String) = Action { implicit request =>
    Oauth2Controller.loggedIn(request) match {
      case Some(a) =>
        Redirect(routes.ApplicationController.index())
      case None =>
        redirectToAuthorization(scope, request)
    }
  }

  def link(scope: String) = Action { implicit request =>
    Oauth2Controller.loggedIn(request) match {
      case Some(a) =>
        redirectToAuthorization(scope, request)
      case None =>
        Unauthorized
    }
  }

  private def redirectToAuthorization(scope: String, request: WrappedRequest) = {
    val state = UUID.randomUUID().toString
    Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
      request.session + (OAuth2StateKey -> state)
    )
  }

  def authorize = Action { implicit request =>
    Forbidden
  }

  def logout = Action.async{ implicit request =>
    //tokenAccessor.extract(request) foreach idContainer.remove
    //result.map(tokenAccessor.delete)
    Redirect(routes.ApplicationController.index)
  }

/*
  override def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth link succeeded with token[$token]")
    retrieveProviderUser(token).map{providerUser =>
      connectivity.getStorage.store(providerUser)
      Redirect(routes.ApplicationController.index)
    }
  }

  override def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth login succeeded with token[$token]")
    retrieveProviderUser(token).flatMap { providerUser =>
      val storage = connectivity.getStorage
      val maybeAccount = storage.getAccount(providerUser.athleteId)
      Logger.info(s"account for token[$token] is $maybeAccount")
      if (maybeAccount.isEmpty) storage.store(providerUser)
      gotoLoginSucceeded(providerUser.athleteId)
    }
  }
*/
}
