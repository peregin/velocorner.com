package controllers


import controllers.auth.AuthConfigSupport
import highcharts.DailySeries
import jp.t2v.lab.play2.auth.{Logout, OptionalAuthElement}
import org.joda.time.LocalDate
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model._
import velocorner.util.Metrics

import scala.concurrent.Future

object Application extends Controller with OptionalAuthElement with AuthConfigSupport with Logout with Metrics {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val log = new slf4s.Logger(Logger.underlying())

  def index = StackAction{ implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val maybeAccount = loggedIn
      Logger.info(s"rendering for $maybeAccount")

      import RefreshStrategy._
      refreshClubActivities() // if needed

      val storage = Global.getStorage
      val currentYear = LocalDate.now().getYear
      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

      val dailyAthleteProgress = storage.dailyProgressForAll(200)
      val mostRecentAthleteProgress = AthleteDailyProgress.keepMostRecentDays(dailyAthleteProgress, 14)

      val clubAthleteIds = storage.getClub(Club.Velocorner).map(_.memberIds).getOrElse(List.empty)
      val clubAthletes = clubAthleteIds.flatMap(id => storage.getAthlete(id))
      val id2Members = clubAthletes.map(a => (a.id.toString, a.firstname.getOrElse(a.id.toString))).toMap
      val seriesId2Name = (ds: DailySeries) => ds.copy(name = id2Members.getOrElse(ds.name, ds.name))

      import highcharts._
      LandingPageContext(
        maybeAccount,
        currentYearStatistics,
        toDistanceSeries(flattenedYearlyProgress),
        toDistanceSeries(aggregatedYearlyProgress),
        toAthleteDistanceSeries(mostRecentAthleteProgress).map(_.aggregate).map(seriesId2Name),
        toAthleteElevationSeries(mostRecentAthleteProgress).map(_.aggregate).map(seriesId2Name)
      )
    }

    Ok(views.html.index(context))
  }

  def refresh = AsyncStack{ implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"refreshing for $maybeAccount")

    import RefreshStrategy._
    maybeAccount.foreach(refreshAccountActivities)

    Future.successful(Redirect(routes.Application.index()))
  }

  def logout = Action.async{ implicit request =>
    gotoLogoutSucceeded
  }

  def about = Action {
    Ok(views.html.about())
  }
}