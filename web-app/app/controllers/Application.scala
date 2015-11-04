package controllers

import org.joda.time.LocalDate
import play.Logger
import play.api.mvc._
import velocorner.model.{Progress, YearlyProgress}
import velocorner.util.Metrics

object Application extends Controller with Metrics {

  def index = Action { implicit request =>
    Logger.info("rendering landing page...")
    val currentYear = LocalDate.now().getYear
    val yearlyProgress = Global.getDataHandler.yearlyProgress
    val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
    val context = LandingPageContext(
      Global.getSecretConfig.getApplicationId,
      aggregatedYearlyProgress.filter(_.year == currentYear).headOption.map(_.progress.last.progress).getOrElse(Progress.zero),
      yearlyProgress,
      aggregatedYearlyProgress
    )
    val progress = timed("getting yearly progress")(Global.getDataHandler.yearlyProgress)
    Ok(views.html.index(context))
  }

  def about = Action {
    Ok(views.html.about())
  }

}