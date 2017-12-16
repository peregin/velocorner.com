package controllers.auth

import javax.inject.Inject

import controllers.ConnectivitySettings
import play.Logger
import play.api.libs.oauth.{ConsumerKey, OAuth, RequestToken, ServiceInfo}
import play.api.mvc._
import play.api.mvc.Results._
import velocorner.model.Account

class WithingsController @Inject()(val connectivity: ConnectivitySettings) {

  val clientToken: String = connectivity.secretConfig.getToken("withings")
  val clientSecret: String = connectivity.secretConfig.getSecret("withings")
  val callbackUrl: String = connectivity.secretConfig.getCallbackUrl("withings")
  val KEY = ConsumerKey(clientToken, clientSecret)

  val OAuthStateKey = "velocorner.oauth.state"

  val oauth = OAuth(ServiceInfo(
    "https://developer.health.nokia.com/account/request_token",
    "https://developer.health.nokia.com/account/access_token",
    "https://developer.health.nokia.com/account/authorize", KEY),
    true)

  def login(scope: String) = Action { implicit request =>
    loggedIn(request) match {
      case Some(a) =>
        Redirect(controllers.routes.ApplicationController.index())
      case None =>
        process(request)
    }
  }

  def authorize = Action { implicit request =>
    Logger.info(s"withings callback with $request request")
    process(request)
  }

  // FIXME: not implemented yet, re-use
  def loggedIn(implicit request: Request[AnyContent]): Option[Account] = None

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  private def process(request: Request[AnyContent]): Result = {
    val res = request.getQueryString("oauth_verifier").map { verifier => // means was from callback
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      oauth.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          Logger.info(s"received authorization token $t")
          Redirect(controllers.routes.ApplicationController.index).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      }
    }.getOrElse(  // means it is triggered from the user
      oauth.retrieveRequestToken(callbackUrl) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Logger.info(s"received request token $t")
          Redirect(oauth.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      })
    res
  }
}
