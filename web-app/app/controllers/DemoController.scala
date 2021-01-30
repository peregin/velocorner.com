package controllers

import controllers.util.{ActivityOps, WebMetrics}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import velocorner.model.Units
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
}
