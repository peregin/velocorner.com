package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import org.joda.time.LocalDate
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model.{Progress, YearlyProgress}
import velocorner.util.Metrics

object Application extends Controller with OptionalAuthElement with AuthConfigSupport with Metrics {

  override val log = new slf4s.Logger(Logger.underlying())

  def index = StackAction { implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val dataHandler = Global.getDataHandler
      val currentYear = LocalDate.now().getYear

      val yearlyProgress = dataHandler.yearlyProgress
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

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

  def oauthCallback(maybeState: Option[String], maybeCode: Option[String]) = Action {
    log.info(s"access token $maybeCode")
    maybeCode match {
      case Some(code) =>
        // token exchange
        val auth = Global.getDataHandler.feed.getOAuth2Token(code, Global.getSecretConfig.getApplicationSecret)
        log.info(s"logged in with token[${auth.access_token}] and athlete ${auth.athlete}")
        Redirect("/")
      case _ =>
        BadRequest("Unable to authorize")
    }
  }

}