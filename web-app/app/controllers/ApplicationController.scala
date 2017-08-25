package controllers


import javax.inject.Inject

import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.util.Metrics

import scala.concurrent.Future

class ApplicationController @Inject()
(components: ControllerComponents, val connectivity: ConnectivitySettings, strategy: RefreshStrategy)
(implicit assets: AssetsFinder) extends AbstractController(components) with Metrics {

  override val log = new slf4s.Logger(Logger.underlying())

  def index = Action{ implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val maybeAccount = Oauth2Controller1.loggedIn(request)
      Logger.info(s"rendering for $maybeAccount")
      PageContext(maybeAccount)
    }

    Ok(views.html.index(context))
  }

  def refresh = Action.async{ implicit request =>
    val maybeAccount = Oauth2Controller1.loggedIn(request)
    Logger.info(s"refreshing for $maybeAccount")

    maybeAccount.foreach(strategy.refreshAccountActivities)

    Future.successful(Redirect(routes.ApplicationController.index()))
  }
}