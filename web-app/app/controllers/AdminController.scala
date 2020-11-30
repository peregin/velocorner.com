package controllers

import cats.data.OptionT
import cats.implicits._
import controllers.auth.AuthChecker
import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.AdminInfo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AdminController @Inject()(val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker {

  // def mapped to /api/admin/status
  def status = AuthAsyncAction { implicit request =>
    val res = for {
      _ <- OptionT(Future(loggedIn.filter(_.isAdmin())))
      adminStorage = connectivity.getStorage.getAdminStorage
      accounts <- OptionT.liftF(adminStorage.countAccounts)
      activeAccounts <- OptionT.liftF(adminStorage.countActiveAccounts)
      activities <- OptionT.liftF(adminStorage.countActivities)
    } yield AdminInfo(
      accounts = accounts,
      activeAccounts = activeAccounts,
      activities = activities
    )
    res.map(info => Ok(Json.toJson(info))).getOrElse(Forbidden)
  }
}
