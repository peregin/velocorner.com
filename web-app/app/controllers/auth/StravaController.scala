package controllers.auth

import java.util.UUID
import java.util.concurrent.Executors

import controllers.ConnectivitySettings
import controllers.auth.StravaController.{AccessToken, ConsumerUser, OAuth2StateKey, ProviderUser, ec}
import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import velocorner.model.Account
import velocorner.util.CloseableResource

import scala.concurrent.{ExecutionContext, Future}

object StravaController {

  // package object?
  type AccessToken = String
  type ProviderUser = Account
  type ConsumerUser = Account
  type User = Account
  type Id = Long
  type AuthenticityToken = String
  type ResultUpdater = Result => Result

  val OAuth2CookieKey = "velocorner.oauth2.id"
  val OAuth2StateKey = "velocorner.oauth2.state"
  val OAuth2AttrKey = TypedKey[Account]

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10, (r: Runnable) => {
    val t = new Thread(r, "play worker")
    t.setDaemon(true)
    t
  }
  ))
}

class StravaController @Inject()(val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with CloseableResource {

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  def login(scope: String) = timed("LOGIN") {
    Action { implicit request =>
      logger.info(s"LOGIN($scope)")
      loggedIn(request) match {
        case Some(account) =>
          Redirect(controllers.routes.WebController.index())
        case None =>
          redirectToAuthorization(scope, request)
      }
    }
  }

  // callback from OAuth2
  def authorize = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code" -> nonEmptyText,
        "state" -> nonEmptyText.verifying(s => request.session.get(OAuth2StateKey).exists(_ == s))
      )
    ).bindFromRequest
    logger.info(s"authorize ${form.data}")

    def formSuccess(v: (String, String)): Future[Result] = {
      val (code, state) = v
      for {
        accessTokenResponse <- authenticator.retrieveAccessToken(code)
        oauthResult <- loggedIn(request) match {
          case Some(account) => onOAuthLinkSucceeded(accessTokenResponse, account)
          case None => onOAuthLoginSucceeded(accessTokenResponse)
        }
      } yield oauthResult
    }

    val result = form.value match {
      case Some(v) if !form.hasErrors => formSuccess(v)
      case _ => Future.successful(BadRequest)
    }
    result.map(_.removingFromSession(OAuth2StateKey))
  }

  def logout = Action { implicit request =>
    logger.info("logout")
    tokenAccessor.extract(request) foreach idContainer.remove
    val result = Redirect(controllers.routes.WebController.index())
    tokenAccessor.delete(result)
  }

  // - utility methods below -

  private def redirectToAuthorization(scope: String, request: Request[AnyContent]) = {
    // TODO: propagate an applications state
    val state = UUID.randomUUID().toString
    Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
      request.session + (OAuth2StateKey -> state)
    )
  }

  // the original API distinguishes between provider and consumer users
  def retrieveProviderUser(token: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    logger.info(s"retrieve provider user for $token")
    for {
      athlete <- withCloseable(connectivity.getStravaFeed(token))(_.getAthlete)
      _ = logger.info(s"got provided athlete for user $athlete")
    } yield Account.from(athlete, token)
  }

  def onOAuthLinkSucceeded(resp: AccessTokenResponse, consumerUser: ConsumerUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    logger.info(s"oauth LINK succeeded with token[${resp.token}]")
    val storage = connectivity.getStorage
    for {
      providerUser <- resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.token))
      accountStorage = storage.getAccountStorage
      _ <- accountStorage.store(providerUser)
    } yield Redirect(controllers.routes.WebController.index())
  }

  // match provider and consumer users
  def onOAuthLoginSucceeded(resp: AccessTokenResponse)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    logger.info(s"oauth LOGIN succeeded with token[${resp.token}]")
    val storage = connectivity.getStorage
    for {
      providerUser <- resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.token))
      accountStorage = storage.getAccountStorage
      consumerUserOpt <- accountStorage.getAccount(providerUser.athleteId)
      freshUser = consumerUserOpt.map { cu =>
        // keep values from the database, like last update and role
        providerUser.copy(lastUpdate = cu.lastUpdate, role = cu.role)
      }.getOrElse(providerUser)
      _ <- accountStorage.store(freshUser)
      token <- idContainer.startNewSession(providerUser.athleteId, sessionTimeoutInSeconds)
      result <- Future.successful(Redirect(controllers.routes.WebController.index()))
    } yield tokenAccessor.put(token)(result)
  }
}
