package controllers

import javax.inject.Inject

import controllers.auth.AuthChecker
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.mvc._
import velocorner.util.Metrics

import scala.concurrent.Future

class ApplicationController @Inject()
(components: ControllerComponents, val cache: SyncCacheApi,
 val connectivity: ConnectivitySettings, strategy: RefreshStrategy)
(implicit assets: AssetsFinder) extends AbstractController(components) with AuthChecker with Metrics {

  def index = AuthAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"rendering landing page for $maybeAccount")
    val context = timed("building page context") {
      PageContext(maybeAccount, connectivity.secretConfig.isWithingsEnabled())
    }
    Ok(views.html.index(context))
  }

  def refresh = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"refreshing for $maybeAccount")
    maybeAccount.foreach(strategy.refreshAccountActivities)
    Future.successful(Redirect(routes.ApplicationController.index()))
  }

  def search = Action {
    Ok(views.html.search(assets))
  }

  def about = Action {
    Ok(views.html.about(assets))
  }
}