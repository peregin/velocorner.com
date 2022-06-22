package controllers

import cats.data.OptionT
import cats.implicits._
import controllers.auth.AuthChecker

import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.AdminInfo
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.BrandSearch
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AdminController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker {

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

  // def mapped to /api/admin/brand/upload
  def brandUpload = Action(parse.multipartFormData) { implicit request =>
    if (loggedIn(request).exists(_.isAdmin())) {
      request.body
        .file("brands")
        .map { payload =>
          // only get the last part of the filename
          // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
          logger.info(s"uploaded ${payload.ref.getAbsolutePath} $payload")
          val brands = JsonIo.readFromGzipFile[List[MarketplaceBrand]](payload.ref.getAbsolutePath)
          logger.info(s"found ${brands.size}")

          val feed = new BrandSearch(connectivity.secretConfig)
          Await.result(feed.bulk(brands), 60.seconds)
          Ok("File uploaded")
        }
        .getOrElse {
          Redirect(routes.WebController.admin).flashing("error" -> "Missing file")
        }
    } else {
      Forbidden
    }
  }
}
