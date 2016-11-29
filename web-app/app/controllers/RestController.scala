package controllers

import highcharts._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import velocorner.model.{AthleteDailyProgress, Club}

import scala.concurrent.Future

/**
  * Created by levi on 06/10/16.
  */
object RestController extends Controller {

  def recentClub(action: String) = Action.async { implicit request =>
    // sync, load if needed
    RefreshStrategy.refreshClubActivities()

    val storage = Global.getStorage
    val dailyAthleteProgress = storage.dailyProgressForAll(200)
    val mostRecentAthleteProgress = AthleteDailyProgress.keepMostRecentDays(dailyAthleteProgress, 14)

    val clubAthleteIds = storage.getClub(Club.Velocorner).map(_.memberIds).getOrElse(List.empty)
    val clubAthletes = clubAthleteIds.flatMap(id => storage.getAthlete(id))
    val id2Members = clubAthletes.map(a => (a.id.toString, a.firstname.getOrElse(a.id.toString))).toMap
    val seriesId2Name = (ds: DailySeries) => ds.copy(name = id2Members.getOrElse(ds.name, ds.name))

    val dataSeries = action match {
      case "distance" => toAthleteDistanceSeries(mostRecentAthleteProgress)
      case "elevation" => toAthleteElevationSeries(mostRecentAthleteProgress)
      case other => sys.error(s"not supported action: $action")
    }
    val series = dataSeries.map(_.aggregate).map(seriesId2Name)
    Future.successful(
      Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(series)))
    )
  }
}
