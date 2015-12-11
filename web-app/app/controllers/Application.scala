package controllers

import jp.t2v.lab.play2.auth.{Logout, OptionalAuthElement}
import org.joda.time.LocalDate
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model.{Progress, YearlyProgress}
import velocorner.util.Metrics

object Application extends Controller with OptionalAuthElement with AuthConfigSupport with Logout with Metrics {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val log = new slf4s.Logger(Logger.underlying())

  def index = StackAction { implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val account = loggedIn
      Logger.info(s"account $account")
      val storage = Global.getStorage
      val currentYear = LocalDate.now().getYear

      val yearlyProgress = YearlyProgress.from(storage.dailyProgress)
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

      LandingPageContext(
        account,
        currentYearStatistics,
        flattenedYearlyProgress,
        aggregatedYearlyProgress
      )
    }

    Ok(views.html.index(context))
  }

  def about = Action {
    Ok(views.html.about())
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }
}