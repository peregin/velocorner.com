package controllers.auth

import java.util.UUID
import java.util.concurrent.Executors
import cats.implicits._
import controllers.ConnectivitySettings
import controllers.auth.StravaController.{ec, OAuth2StateKey}

import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.mvc._
import velocorner.model.Account
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

  implicit val ec = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(5, (r: Runnable) => new Thread(r, "play worker") <| (_.setDaemon(true)))
  )
}

class StravaController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker {

  protected val authenticator: StravaAuthenticator = new StravaAuthenticator(connectivity)

  // called from FE web-app ONLY
  def login(scope: String) = timed("LOGIN") {
    Action { implicit request =>
      logger.info(s"LOGIN($scope)")
      loggedIn(request) match {
        case Some(_) =>
          // already logged in
          Redirect(controllers.routes.WebController.index)
        case None =>
          // authorize
          // - state is use to validate redirect from the OAuth provider with a unique identifier
          // - scope is the host from FE
          val state = UUID.randomUUID().toString
          Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
            request.session + (OAuth2StateKey -> state)
          )
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
    val result = Redirect(controllers.routes.WebController.index)
    tokenAccessor.delete(result)
  }

  // - utility methods below -

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
    logger.info(s"oauth LINK succeeded with token[${resp.accessToken}] and user[${consumerUser.athleteId}]")
    for {
      _ <- login(resp, consumerUser.some)
    } yield Redirect(controllers.routes.WebController.index)
  }

  // matches the provider and consumer users
  def onOAuthLoginSucceeded(resp: OAuth2TokenResponse)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
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
  private def login(resp: OAuth2TokenResponse, consumerUser: Option[ConsumerUser])(implicit ctx: ExecutionContext): Future[ProviderUser] =
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
      _ <- bikes.traverse(storage.getGearStorage.store(_, Gear.Bike))
      shoes = athlete.shoes.getOrElse(Nil)
      _ <- shoes.traverse(storage.getGearStorage.store(_, Gear.Shoe))
      _ = logger.info(s"athlete[${athlete.id}] has ${bikes.size} bikes and ${shoes.size} shoes")
    } yield athlete
}