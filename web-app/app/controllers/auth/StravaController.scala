package controllers.auth

import cats.data.OptionT

import java.util.UUID
import java.util.concurrent.Executors
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import controllers.ConnectivitySettings
import controllers.auth.StravaController.{ec, OAuth2StateKey}

import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import velocorner.api.Account
import velocorner.model.strava.Gear
import velocorner.feed.OAuth2._

import scala.concurrent.{ExecutionContext, Future}
// for kestrel combinator
import mouse.all._

object StravaController {

  type User = Account
  type Id = Long
  type AuthenticityToken = String
  type ResultUpdater = Result => Result

  // verifies the code between the session and submitted form
  val OAuth2StateKey = "velocorner.oauth2.state"

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(5, (r: Runnable) => new Thread(r, "play worker") <| (_.setDaemon(true)))
  )
}

class StravaController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker
    with LazyLogging {

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  // called from Play Framework FE web-app ONLY
  def login(): Action[AnyContent] = timed("LOGIN") {
    Action { implicit request =>
      logger.info(s"LOGIN started...")
      loggedIn(request) match {
        case Some(_) =>
          // already logged in
          Redirect(controllers.routes.WebController.index)
        case None =>
          // authorize
          // - state is use to validate redirect from the OAuth provider with a unique identifier
          val host = request.host
          val state = UUID.randomUUID().toString
          Redirect(authenticator.getAuthorizationUrl(host, state)).withSession(
            request.session + (OAuth2StateKey -> state)
          )
      }
    }
  }

  // callback from OAuth2
  def authorize: Action[AnyContent] = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code" -> nonEmptyText,
        "state" -> nonEmptyText.verifying(s => request.session.get(OAuth2StateKey).contains(s))
      )
    ).bindFromRequest()
    logger.info(s"authorize ${form.data}")

    def formSuccess(v: (String, String)): Future[Result] = {
      val (code, _) = v
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

  // bound to /api/token/strava
  // called by FE to after from the Strava callback to exchange the access code with access and refresh tokens
  def feToken: Action[AnyContent] = Action.async { implicit request =>
    (for {
      code <- OptionT[Future, String](Future.successful(request.getQueryString("code")))
      _ = logger.info(s"requesting token $code")
      resp <- OptionT.liftF(authenticator.retrieveAccessToken(code))
      athlete <- OptionT.liftF(login(resp, none))
      jwt = JwtUser.toJwtUser(athlete)
      token = jwt.toToken(connectivity.secretConfig.getJwtSecret)
    } yield Ok(Json.obj("access_token" -> JsString(token)))).getOrElse(BadRequest)
  }

  // bound to /api/logout/strava
  def feLogout: Action[AnyContent] = Action { implicit request =>
    logger.info("logout")
    tokenAccessor.extract(request) foreach idContainer.remove
    tokenAccessor.delete(Ok)
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    logger.info("logout")
    tokenAccessor.extract(request) foreach idContainer.remove
    val result = Redirect(controllers.routes.WebController.index)
    tokenAccessor.delete(result)
  }

  // - utility methods below -

  // API distinguishes between provider and consumer users
  // the provider user is the Strava Athlete here
  // consumer user is the account
  def retrieveProviderUser(token: AccessToken): Future[ProviderUser] = {
    logger.info(s"retrieve provider user for $token")
    val feed = connectivity.getStravaFeed(token)
    feed.getAthlete <| (_.onComplete(_ => feed.close()))
  }

  def onOAuthLinkSucceeded(resp: OAuth2TokenResponse, consumerUser: ConsumerUser): Future[Result] = {
    logger.info(s"oauth LINK succeeded with token[${resp.accessToken}] and user[${consumerUser.athleteId}]")
    for {
      _ <- login(resp, consumerUser.some)
    } yield Redirect(controllers.routes.WebController.index)
  }

  // matches the provider and consumer users
  def onOAuthLoginSucceeded(resp: OAuth2TokenResponse): Future[Result] = {
    logger.info(s"oauth LOGIN succeeded with token[${resp.accessToken}]")
    for {
      athlete <- login(resp, none)
      token <- idContainer.startNewSession(athlete.id, sessionTimeoutInSeconds)
      result <- Future.successful(Redirect(controllers.routes.WebController.index))
    } yield tokenAccessor.put(token)(result)
  }

  // same functionality when the account is linked with a given consumerUser or logged in to an existing mapping
  // consumerUser - when empty means a new login
  // consumerUser - when present links the login with the given account
  private def login(resp: OAuth2TokenResponse, consumerUser: Option[ConsumerUser]): Future[ProviderUser] =
    for {
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
      bikes = athlete.bikes.getOrElse(Nil)
      _ <- bikes.traverse(storage.getGearStorage.store(_, Gear.Bike, account.athleteId))
      shoes = athlete.shoes.getOrElse(Nil)
      _ <- shoes.traverse(storage.getGearStorage.store(_, Gear.Shoe, account.athleteId))
      _ = logger.info(s"athlete[${athlete.id}] has ${bikes.size} bikes and ${shoes.size} shoes")
    } yield athlete
}
