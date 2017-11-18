package controllers


import javax.inject.Inject

import org.slf4s
import play.Logger
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import velocorner.util.Metrics

import scala.concurrent.Future

class ApplicationController @Inject()
(components: ControllerComponents, val cache: AsyncCacheApi,
 val connectivity: ConnectivitySettings, strategy: RefreshStrategy)
(implicit assets: AssetsFinder) extends AbstractController(components) with AuthChecker with Metrics {

  override val log = new slf4s.Logger(Logger.underlying()) // because of the Metrics

  def index = AuthAction { implicit request =>
    val maybeAccount = loggedIn(request)
    Logger.info(s"rendering landing page for $maybeAccount")

    val context = timed("building page context") {
      PageContext(maybeAccount)
    }

    Ok(views.html.index(context))
  }

  def refresh = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn(request)
    Logger.info(s"refreshing for $maybeAccount")

    maybeAccount.foreach(strategy.refreshAccountActivities)

    Future.successful(Redirect(routes.ApplicationController.index()))
  }
}