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
      val dataHandler = Global.getDataHandler
      val currentYear = LocalDate.now().getYear

      val yearlyProgress = dataHandler.yearlyProgress
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.filter(_.year == currentYear).headOption.map(_.progress.last.progress).getOrElse(Progress.zero)

      LandingPageContext(
        currentYearStatistics,
        flattenedYearlyProgress,
        aggregatedYearlyProgress,
        dataHandler.feed.getOAuth2Url(request.host)
      )
    }

    Ok(views.html.index(context))
  }

  def about = Action {
    Ok(views.html.about())
  }

  def oauthCallback(maybeCode: Option[String], maybeState: Option[String]) = Action {
    log.info(s"access token $maybeCode")
    maybeCode match {
      case Some(code) =>
        // token exchange
        //Global.getDataHandler.feed.getOAuth2Token(code, )
        Redirect("/")
      case _ =>
        BadRequest("Unable to authorize")
    }
  }

}