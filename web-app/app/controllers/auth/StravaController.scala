package controllers.auth

import java.util.UUID
import java.util.concurrent.Executors
import cats.implicits._
import controllers.ConnectivitySettings
import controllers.auth.StravaController.{OAuth2StateKey, ec}
import pdi.jwt.JwtJson

import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.json.Json
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import velocorner.model.Account
import velocorner.model.strava.Gear
import velocorner.feed.OAuth2._

import scala.concurrent.{ExecutionContext, Future}
//for kestrel combinator
import mouse.all._

object StravaController {

  type User = Account
  type Id = Long
  type AuthenticityToken = String
  type ResultUpdater = Result => Result

  val OAuth2CookieKey = "velocorner.oauth2.id"
  val OAuth2StateKey = "velocorner.oauth2.state"
  val OAuth2AttrKey = TypedKey[Account]

  implicit val ec =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10, (r: Runnable) => new Thread(r, "play worker") <| (_.setDaemon(true))))
}

class StravaController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker {

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  // from FE web
  def login(scope: String) = timed("LOGIN") {
    Action { implicit request =>
      logger.info(s"LOGIN($scope)")
      loggedIn(request) match {
        case Some(_) =>
          Redirect(controllers.routes.WebController.index()) // already logged in
        case None =>
          redirectToAuthorization(scope, request) // authorize
      }
    }
  }

  // from FE api
  def apiLogin = Action { implicit request =>
    logger.info(s"API LOGIN")
    loggedIn(request) match {
      case Some(account) =>
        logger.info("already logged in")
        val token = JwtUser.toJwtUser(account).toToken()
        logger.info(s"token = $token")
        Ok(token)
      case None =>
        // state is use to validate redirect from the OAuth provider with a unique identifier
        val state = UUID.randomUUID().toString
        val scope = "localhost"
        // ws.url(...).get
        Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
          request.session + (OAuth2StateKey -> state)
        )
      //Forbidden
    }
  }

  // callback from OAuth2
  def authorize = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code" -> nonEmptyText,
        "state" -> nonEmptyText.verifying(s => request.session.get(OAuth2StateKey).exists(_ == s))
      )
    ).bindFromRequest()
    logger.info(s"authorize ${form.data}")

    def formSuccess(v: (String, String)): Future[Result] = {
      val (code, state) = v
      for {
        accessTokenResponse <- authenticator.retrieveAccessToken(code)
        oauthResult <- loggedIn(request) match {
          case Some(account) => onOAuthLinkSucceeded(accessTokenResponse, account)
          case None          => onOAuthLoginSucceeded(accessTokenResponse)
        }
      } yield oauthResult
    }

    val result = form.value match {
      case Some(v) if !form.hasErrors => formSuccess(v)
      case _                          => Future.successful(BadRequest)
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
    // state is use to validate redirect from the OAuth provider with a unique identifier
    val state = UUID.randomUUID().toString
    Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
      request.session + (OAuth2StateKey -> state)
    )
  }

  // API distinguishes between provider and consumer users
  // the provider user is the Strava Athlete here
  // consumer user is the account
  def retrieveProviderUser(token: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    logger.info(s"retrieve provider user for $token")
    val feed = connectivity.getStravaFeed(token)
    feed.getAthlete <| (_.onComplete(_ => feed.close()))
  }

  def onOAuthLinkSucceeded(resp: OAuth2TokenResponse, consumerUser: ConsumerUser)(implicit
      request: RequestHeader,
      ctx: ExecutionContext
  ): Future[Result] = {
    logger.info(s"oauth LINK succeeded with token[${resp.accessToken}]")
    for {
      _ <- login(resp, consumerUser.some)
    } yield Redirect(controllers.routes.WebController.index())
  }

  // matches the provider and consumer users
  def onOAuthLoginSucceeded(resp: OAuth2TokenResponse)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    logger.info(s"oauth LOGIN succeeded with token[${resp.accessToken}]")
    for {
      athlete <- login(resp, none)
      token <- idContainer.startNewSession(athlete.id, sessionTimeoutInSeconds)
      result <- Future.successful(Redirect(controllers.routes.WebController.index()))
    } yield tokenAccessor.put(token)(result)
  }

  // same functionality when the account is linked with a given consumerUser or logged in to an existing mapping
  private def login(resp: OAuth2TokenResponse, consumerUser: Option[ConsumerUser])(implicit ctx: ExecutionContext): Future[ProviderUser] =
    for {
      //athlete <- resp.athlete.map(Future.successful).getOrElse(retrieveProviderUser(resp.accessToken))
      // retrieve the provider user has more details than the user got at authentication
      athlete <- retrieveProviderUser(resp.accessToken)
      storage = connectivity.getStorage
      maybeAccount <- consumerUser.fold(storage.getAccountStorage.getAccount(athlete.id))(_ => Future(consumerUser))
      _ = assert(consumerUser.forall(_.athleteId == athlete.id))
      // keep values from the database, like last update and role
      account = Account.from(
        athlete,
        resp.toStravaAccess,
        lastUpdate = maybeAccount.flatMap(_.lastUpdate),
        role = maybeAccount.flatMap(_.role),
        unit = maybeAccount.flatMap(_.unit)
      )
      _ = logger.info(s"last updated at ${account.lastUpdate.mkString}")
      _ <- storage.getAccountStorage.store(account)
      _ <- athlete.bikes.getOrElse(Nil).traverse(storage.getGearStorage.store(_, Gear.Bike))
      _ <- athlete.shoes.getOrElse(Nil).traverse(storage.getGearStorage.store(_, Gear.Shoe))
    } yield athlete
}
