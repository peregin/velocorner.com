package controllers.auth

import cats.implicits._
import controllers.ConnectivitySettings
import controllers.auth.StravaController.{ec, OAuth2StateKey}
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, optional, text, tuple}
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import velocorner.feed.OAuth2._
import velocorner.model.Account
import velocorner.model.strava.Gear

import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject
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
    Action.async { implicit request =>
      logger.info(s"LOGIN($scope)")
      loggedIn(request) match {
        // already logged in
        case Some(account) =>
          logger.info(s"already logged in ${account.athleteId}")
          Future.successful(Redirect(controllers.routes.WebController.index))

        // not logged, in call OAuth2 provider
        case None =>
          // authorize - scope is the host from FE - calls Strava with the callback url (authorize)
          val state = UUID.randomUUID().toString
          val result = Redirect(authenticator.getAuthorizationUrl(scope, state.some)).withSession(
            request.session + (OAuth2StateKey -> state)
          )

          val token = "" // authenticator.authorize(authenticator.getAuthorizationUrl(scope, state.some))
          val jwtUser = JwtUser.fromToken(token)(connectivity.secretConfig.getJwtSecret)
          for {
            sessionToken <- idContainer.startNewSession(jwtUser.id, sessionTimeoutInSeconds)
            _ = tokenAccessor.put(sessionToken)(result)
          } yield result.removingFromSession(OAuth2StateKey)
      }
    }
  }

  // callback from OAuth2 (triggered by Strava from web-app or web-front)
  // creates the server side session and JWT token
  // the caller must redirect to the proper page
  def authorize = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code" -> nonEmptyText,
        "state" -> optional(text).verifying(_.forall(s => request.session.get(OAuth2StateKey).exists(_ == s)))
      )
    ).bindFromRequest()
    logger.info(s"authorize ${form.data}")

    def formSuccess(code: String): Future[Result] = for {
      accessTokenResponse <- authenticator.retrieveAccessToken(code)
      _ = logger.info(s"OAUTH succeeded with access token[${accessTokenResponse.accessToken}]")
      account <- login(accessTokenResponse, none)
      jwtUser = JwtUser.toJwtUser(account)
      jwt = jwtUser.toToken(connectivity.secretConfig.getJwtSecret)
      _ = logger.info(s"logging in as ${account.athleteId}")
    } yield Ok(Json.obj("token" -> JsString(jwt)))

    form.value match {
      case Some(v) if !form.hasErrors =>
        val (code, _) = v
        formSuccess(code)
      case _ =>
        Future.successful[Result](Unauthorized)
    }
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

  // same functionality when the account is linked with a given consumerUser or logged in to an existing mapping
  // consumerUser - when empty means a new login
  // consumerUser - when present links the login with the given account
  private def login(resp: OAuth2TokenResponse, consumerUser: Option[ConsumerUser])(implicit ctx: ExecutionContext): Future[Account] =
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
      _ <- athlete.bikes.getOrElse(Nil).traverse(storage.getGearStorage.store(_, Gear.Bike))
      _ <- athlete.shoes.getOrElse(Nil).traverse(storage.getGearStorage.store(_, Gear.Shoe))
    } yield account
}
