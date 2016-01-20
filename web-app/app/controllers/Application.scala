package controllers

import jp.t2v.lab.play2.auth.{Logout, OptionalAuthElement}
import org.joda.time.LocalDate
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model.{Progress, YearlyProgress}
import velocorner.util.Metrics

import scala.concurrent.Future

object Application extends Controller with OptionalAuthElement with AuthConfigSupport with Logout with Metrics {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val log = new slf4s.Logger(Logger.underlying())

  def index = StackAction{ implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val maybeAccount = loggedIn
      Logger.info(s"account $maybeAccount")
      val storage = Global.getStorage
      val currentYear = LocalDate.now().getYear

      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgress(account.athleteId))).getOrElse(Iterable.empty)
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

      LandingPageContext(
        maybeAccount,
        currentYearStatistics,
        flattenedYearlyProgress,
        aggregatedYearlyProgress
      )
    }

    Ok(views.html.index(context))
  }

  def refresh = AsyncStack{ implicit request =>
    Logger.info("refreshing fom Strava feed...")
    Future.successful(Redirect(routes.Application.index()))
  }

  def logout = Action.async{ implicit request =>
    gotoLogoutSucceeded
  }

  def about = Action {
    Ok(views.html.about())
  }
}