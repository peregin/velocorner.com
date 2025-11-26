package controllers

import cats.data.OptionT
import cats.implicits._
import controllers.auth.AuthChecker
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.Account
import velocorner.api.Account._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfileController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker {

  // def mapped to PUT /api/athletes/me
  def me(): Action[AnyContent] = AuthAction(parse.default) { implicit request =>
    loggedIn match {
      // do not send back Strava access information
      case Some(account) => Ok(Json.toJson(account.copy(stravaAccess = None)))
      case None          => Unauthorized
    }
  }

  // def mapped to PUT /api/athletes/units
  def unit(unit: String): Action[AnyContent] = AuthAsyncAction(parse.default) { implicit request =>
    // validate unit
    val newUnit = Account.convert(unit)
    val res = for {
      account <- OptionT(Future(loggedIn))
      accountStorage = connectivity.getStorage.getAccountStorage
      _ <- OptionT.liftF(accountStorage.store(account.copy(unit = newUnit.some)))
    } yield ()
    res.map(_ => Ok).getOrElse(Unauthorized)
  }
}
