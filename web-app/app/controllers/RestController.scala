package controllers

import javax.inject.Inject

import controllers.auth.AuthChecker
import highcharts._
import org.joda.time.LocalDate
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import velocorner.model._

import scala.concurrent.Future

/**
  * Created by levi on 06/10/16.
  */
class RestController @Inject()(val cache: SyncCacheApi, val connectivity: ConnectivitySettings, strategy: RefreshStrategy)
  extends Controller with AuthChecker {

  // mapped to /rest/club/:action
  def recentClub(action: String) = Action.async { implicit request =>
    Logger.info(s"recent club action for $action")

    // sync, load if needed
    strategy.refreshClubActivities(Club.Velocorner)

    val storage = connectivity.getStorage
    val dailyAthleteProgress = storage.dailyProgressForAll(200)
    val mostRecentAthleteProgress = AthleteDailyProgress.keepMostRecentDays(dailyAthleteProgress, 14)

    val clubAthleteIds = storage.getClub(Club.Velocorner).map(_.memberIds).getOrElse(List.empty)
    val clubAthletes = clubAthleteIds.flatMap(id => storage.getAthlete(id))
    val id2Members = clubAthletes.map(a => (a.id.toString, a.firstname.getOrElse(a.id.toString))).toMap
    val seriesId2Name = (ds: DailySeries) => ds.copy(name = id2Members.getOrElse(ds.name, ds.name))

    val dataSeries = action.toLowerCase match {
      case "distance" => toAthleteDistanceSeries(mostRecentAthleteProgress)
      case "elevation" => toAthleteElevationSeries(mostRecentAthleteProgress)
      case other => sys.error(s"not supported action: $action")
    }
    val series = dataSeries.map(_.aggregate).map(seriesId2Name)
    Future.successful(
      Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(series)))
    )
  }

  // def mapped to /rest/athlete/progress
  // current year's progress
  def statistics = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"athlete statistics for ${maybeAccount.map(_.displayName)}")

    val storage = connectivity.getStorage
    val currentYear = LocalDate.now().getYear
    val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
    val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
    val currentYearProgress = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

    Future.successful(
      Ok(Json.obj("status" ->"OK", "progress" -> Json.toJson(currentYearProgress)))
    )
  }

  // def mapped to /rest/athlete/yearly/:action
  def yearly(action: String) = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"athlete yearly statistics for ${maybeAccount.map(_.displayName)}")

    val storage = connectivity.getStorage
    val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)

    val dataSeries = action.toLowerCase match {
      case "heatmap" => toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress))
      case "distance" => toDistanceSeries(YearlyProgress.aggregate(yearlyProgress))
      case "elevation" => toElevationSeries(YearlyProgress.aggregate(yearlyProgress))
      case other => sys.error(s"not supported action: $other")
    }

    Future.successful(
      Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries)))
    )
  }

  // year to date aggregation
  // def mapped to /rest/athlete/ytd/:action
  def ytd(action: String) = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    val now = LocalDate.now()
    Logger.info(s"athlete year to date $now statistics for ${maybeAccount.map(_.displayName)}")

    val storage = connectivity.getStorage
    val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
    val ytdProgress = yearlyProgress.map(_.ytd(now)).map(ytd =>
      YearlyProgress(ytd.year, Seq(DailyProgress(LocalDate.parse(s"${ytd.year}-01-01"), ytd.progress.map(_.progress).foldLeft(Progress.zero)(_ + _)))))

    val dataSeries = action.toLowerCase match {
      case "distance" => toDistanceSeries(ytdProgress)
      case "elevation" => toElevationSeries(ytdProgress)
      case other => sys.error(s"not supported action: $other")
    }

    Future.successful(
      Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries)))
    )
  }
}
