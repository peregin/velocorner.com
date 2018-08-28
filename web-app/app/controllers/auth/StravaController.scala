package controllers.auth

import java.util.UUID
import java.util.concurrent.{Executors, ThreadFactory}
import javax.inject.Inject

import controllers.ConnectivitySettings
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import velocorner.model.Account
import velocorner.util.CloseableResource

import scala.concurrent.{ExecutionContext, Future}

import controllers.auth.StravaController.{AccessToken, ConsumerUser, OAuth2StateKey, ProviderUser, ec}

object StravaController {

  // package object?
  type AccessToken = String
  type ProviderUser = Account
  type ConsumerUser = Account
  type User = Account
  type Id = Long
  type AuthenticityToken = String
  type SignedToken = String
  type ResultUpdater = Result => Result

  val OAuth2CookieKey = "velocorner.oauth2.id"
  val OAuth2StateKey = "velocorner.oauth2.state"
  val OAuth2AttrKey = TypedKey[Account]

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10, new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r, "play worker")
      t.setDaemon(true)
      t
    }}
  ))
}

class StravaController @Inject()(val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with CloseableResource {

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  def login(scope: String) = Action { implicit request =>
    loggedIn(request) match {
      case Some(a) =>
        Redirect(controllers.routes.ApplicationController.index())
      case None =>
        redirectToAuthorization(scope, request)
    }
  }

  def link(scope: String) = Action { implicit request =>
    loggedIn(request) match {
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
      loggedIn(request) match {
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
    tokenAccessor.extract(request) foreach idContainer.remove
    val res = Redirect(controllers.routes.ApplicationController.index)
    tokenAccessor.delete(res)

  }

  // - utility methods below -

  private def redirectToAuthorization(scope: String, request: Request[AnyContent]) = {
    // TODO: propagate an applications state
    val state =  UUID.randomUUID().toString
    Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
      request.session + (OAuth2StateKey -> state)
    )
  }

  // the original API distinguishes between provider and consumer users
  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    val token = accessToken.toString
    Logger.info(s"retrieve provider user for $token")
    val athlete = withCloseable(connectivity.getFeed(token))(_.getAthlete)
    Logger.info(s"got provided athlete for user $athlete")
    Future.successful(Account.from(athlete, token, None))
  }

  def onOAuthLinkSucceeded(resp: AccessTokenResponse, consumerUser: ConsumerUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Logger.info(s"oauth link succeeded with token[${resp.token}]")
    val providerUserFuture = resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.token))
    providerUserFuture.map{providerUser =>
      connectivity.getStorage.store(providerUser)
      Redirect(controllers.routes.ApplicationController.index)
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
    for {
      token <- idContainer.startNewSession(athleteId, sessionTimeoutInSeconds)
      r     <- loginSucceeded(request)
    } yield tokenAccessor.put(token)(r)
  }

  // auth control
  def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(controllers.routes.ApplicationController.index()))
  }
}
