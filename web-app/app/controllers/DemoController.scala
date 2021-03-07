package controllers

import controllers.util.{ActivityOps, WebMetrics}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.chart.DailyPoint
import velocorner.model.{DailyProgress, Units}
import velocorner.util.DemoActivityUtils

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DemoController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with ActivityOps
    with WebMetrics {

  // demo ytd data
  // route mapped to /api/demo/statistics/ytd/:action/:activity
  def ytdStatistics(action: String, activity: String) = Action.async {
    timedRequest(s"demo ytd data for $action") { implicit request =>
      val activities = DemoActivityUtils.generate()
      val now = LocalDate.now()
      val series = toSumYtdSeries(activities, now, action, Units.Metric)
      Future(Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(series))))
    }
  }

  // demo yearly progress data
  // route mapped to /api/demo/statistics/yearly/:action/:activity
  def yearlyStatistics(action: String, activity: String) = Action.async {
    val activities = DemoActivityUtils.generate()
    val series = toYearlySeries(activities, action, Units.Metric)
    Future(Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(series))))
  }

  // all daily activity list for the last 12 months, shown in the calendar heatmap
  // route mapped to /api/demo/statistics/daily/:action
  def dailyStatistics(action: String): Action[AnyContent] = Action.async { implicit request =>
    val activities = DemoActivityUtils.generate()
    val dailyProgress = DailyProgress.from(activities)
    val series = action.toLowerCase match {
      case "distance" => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(Units.Metric).distance))
      case "elevation" => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(Units.Metric).elevation))
      case other => sys.error(s"not supported action: $other")
    }
    Future(Ok(Json.toJson(series)))
  }
}
