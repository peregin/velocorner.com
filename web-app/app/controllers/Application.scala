package controllers

import org.joda.time.LocalDate
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model.{Progress, YearlyProgress}
import velocorner.util.Metrics

object Application extends Controller with Metrics {

  override val log = new slf4s.Logger(Logger.underlying())

  def index = Action { implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val currentYear = LocalDate.now().getYear
      val yearlyProgress = Global.getDataHandler.yearlyProgress
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.filter(_.year == currentYear).headOption.map(_.progress.last.progress).getOrElse(Progress.zero)

      LandingPageContext(
        Global.getSecretConfig.getApplicationId,
        currentYearStatistics,
        yearlyProgress,
        aggregatedYearlyProgress
      )
    }

    Ok(views.html.index(context))
  }

  def about = Action {
    Ok(views.html.about())
  }

}