package controllers

import java.util.UUID
import java.util.concurrent.{Executors, ThreadFactory}
import javax.inject.Inject

import controllers.auth.{AccessTokenResponse, AuthConfigSupport, StravaAuthenticator}
import jp.t2v.lab.play2.auth.ResultUpdater
import play.Logger
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.typedmap.TypedKey
import play.api.mvc.Results._
import play.api.mvc._
import velocorner.model.Account

import scala.concurrent.{ExecutionContext, Future}


object Oauth2Controller1 {

  val OAuth2StateKey = "velocorner.oauth2.state"
  val OAuth2CookieKey = "velocorner.oauth2.cookie"
  val OAuth2AttrKey = TypedKey[Account]

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10, new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r, "play worker")
      t.setDaemon(true)
      t
    }}
  ))


  def loggedIn(request: Request[AnyContent]): Option[Account] = request.attrs.get[Account](OAuth2AttrKey)

  // TODO: update session and cookie
//  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
//    implicit val (r, ctx) = (req, StackActionExecutionContext(req))
//    val maybeUserFuture = restoreUser.recover { case _ => None -> identity[Result] _ }
//    maybeUserFuture.flatMap { case (maybeUser, cookieUpdater) =>
//      super.proceed(maybeUser.map(u => req.set(OAuth2AttrKey, u)).getOrElse(req))(f).map(cookieUpdater)
//    }
//  }
}

import controllers.Oauth2Controller1.{OAuth2CookieKey, OAuth2StateKey, ec}

class AuthController @Inject()(val connectivity: ConnectivitySettings) extends AuthConfigSupport {

  type AccessToken = String
  type ProviderUser = Account
  type ConsumerUser = Account

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  def login(scope: String) = Action { implicit request =>
    Oauth2Controller1.loggedIn(request) match {
      case Some(a) =>
        Redirect(routes.ApplicationController.index())
      case None =>
        redirectToAuthorization(scope, request)
    }
  }

  def link(scope: String) = Action { implicit request =>
    Oauth2Controller1.loggedIn(request) match {
      case Some(a) =>
        redirectToAuthorization(scope, request)
      case None =>
        Unauthorized
    }
  }

  def authorize = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code"  -> nonEmptyText,
        "state" -> nonEmptyText.verifying(s => request.session.get(OAuth2StateKey).exists(_ == s))
      )
    ).bindFromRequest
    Logger.info(s"authorize request with ${form.data}")

    def formSuccess(v: (String, String)): Future[Result] = {
      val (code, state) = v
      val accessTokenResponse = authenticator.retrieveAccessToken(code)
      Oauth2Controller1.loggedIn(request) match {
        case Some(account) => accessTokenResponse.flatMap(resp => onOAuthLinkSucceeded(resp, account))
        case None => accessTokenResponse.flatMap(onOAuthLoginSucceeded)
      }
    }

    val result = form.value match {
      case Some(v) if !form.hasErrors => formSuccess(v)
      case _ => Future.successful(BadRequest)
    }
    result.map(_.removingFromSession(OAuth2StateKey))
  }

  def logout = Action { implicit request =>
    // TODO: remove it from attributes/session
    Redirect(routes.ApplicationController.index).discardingCookies(DiscardingCookie(OAuth2CookieKey))
  }

  // - utility methods below -

  private def redirectToAuthorization(scope: String, request: Request[AnyContent]) = {
    // TODO: propagate an applications state
    val state =  UUID.randomUUID().toString
    Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
      request.session + (OAuth2StateKey -> state)
    )
  }

  // AsyncAuth
  private def extractToken(request: RequestHeader): Option[String] = tokenAccessor.extract(request)
  private def restoreUser(implicit request: RequestHeader, context: ExecutionContext): Future[(Option[User], ResultUpdater)] = {
    (for {
      token  <- extractToken(request)
    } yield for {
      Some(userId) <- idContainer.get(token)
      Some(user)   <- resolveUser(userId)
      _            <- idContainer.prolongTimeout(token, sessionTimeoutInSeconds)
    } yield {
      Option(user) -> tokenAccessor.put(token) _
    }) getOrElse {
      Future.successful(Option.empty -> identity)
    }
  }

  // the original API distinguishes between provider and consumer users
  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    val token = accessToken.toString
    Logger.info(s"retrieve provider user for $token")
    val athlete = connectivity.getFeed(token).getAthlete
    Logger.info(s"got provided athlete for user $athlete")
    Future.successful(Account.from(athlete, token, None))
  }

  def onOAuthLinkSucceeded(resp: AccessTokenResponse, consumerUser: ConsumerUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth link succeeded with token[${resp.token}]")
    val providerUserFuture = resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.token))
    providerUserFuture.map{providerUser =>
      connectivity.getStorage.store(providerUser)
      Redirect(routes.ApplicationController.index)
    }
  }

  def onOAuthLoginSucceeded(resp: AccessTokenResponse)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth login succeeded with token[${resp.token}]")
    val providerUserFuture = resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.token))
    providerUserFuture.flatMap { providerUser =>
      val storage = connectivity.getStorage
      val maybeAccount = storage.getAccount(providerUser.athleteId)
      Logger.info(s"account for token[${resp.token}] is $maybeAccount")
      if (maybeAccount.isEmpty) storage.store(providerUser)
      gotoLoginSucceeded(providerUser.athleteId)
    }
  }

  def gotoLoginSucceeded(athleteId: Int)(implicit request: RequestHeader): Future[Result] = {
    // new session
    // persist token -> user
    loginSucceeded(request)
  }
}
