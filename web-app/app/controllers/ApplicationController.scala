package controllers


import javax.inject.Inject

import controllers.auth.AuthConfigSupport
import jp.t2v.lab.play2.auth.{Logout, OptionalAuthElement}
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.util.Metrics

import scala.concurrent.Future

class ApplicationController @Inject()(val connectivity: ConnectivitySettings, strategy: RefreshStrategy) extends Controller with OptionalAuthElement with AuthConfigSupport with Logout with Metrics {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val log = new slf4s.Logger(Logger.underlying())

  def index = StackAction{ implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val maybeAccount = loggedIn
      Logger.info(s"rendering for $maybeAccount")
      PageContext(maybeAccount)
    }

    Ok(views.html.index(context))
  }

  def refresh = AsyncStack{ implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"refreshing for $maybeAccount")

    maybeAccount.foreach(strategy.refreshAccountActivities)

    Future.successful(Redirect(routes.ApplicationController.index()))
  }

  def logout = Action.async{ implicit request =>
    gotoLogoutSucceeded
  }

  def about = Action {
    Ok(views.html.about())
  }
}