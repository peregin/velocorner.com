package controllers

import cats.data.OptionT
import cats.implicits._
import controllers.auth.AuthChecker
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.AdminInfo
import velocorner.feed.ProductCrawlerFeed

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdminController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with AuthChecker {

  lazy val productFeed = new ProductCrawlerFeed(connectivity.secretConfig)
  private lazy val adminStorage = connectivity.getStorage.getAdminStorage

  // def mapped to /api/admin/status
  def status: Action[AnyContent] = AuthAsyncAction(parse.default) { implicit request =>
    val res = for {
      _ <- OptionT(Future(loggedIn.filter(_.isAdmin())))
      adminInfo <- OptionT.liftF(retrieveAdminInfo())
    } yield adminInfo
    res.map(info => Ok(Json.toJson(info))).getOrElse(Forbidden)
  }

  private def retrieveAdminInfo(): Future[AdminInfo] = for {
    accounts <- adminStorage.countAccounts
    activeAccounts <- adminStorage.countActiveAccounts
    activities <- adminStorage.countActivities
//    brands <- brandFeed.countBrands().recover { case error =>
//      logger.info(s"failed while counting brands: ${error.getMessage}") // zinc is disabled, since we have a crawler
//      0L
//    }
//    markets <- productFeed.supported().map(_.size.toLong).recover { case error =>
//      logger.error(s"failed while counting markets: ${error.getMessage}")
//      0L
//    }
  } yield AdminInfo(
    accounts = accounts,
    activeAccounts = activeAccounts,
    activities = activities,
    brands = -1,
    markets = -1
  )
}
