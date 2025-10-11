package controllers

import controllers.util.{ActivityOps, WebMetrics}
import model.apexcharts
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.Units
import velocorner.api.chart.DailyPoint
import velocorner.api.wordcloud.WordCloud
import velocorner.model.DailyProgress
import velocorner.util.{DemoActivityUtils, JsonIo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DemoController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with ActivityOps
    with WebMetrics {

  // demo ytd data
  // route mapped to /api/demo/statistics/ytd/:action/:activity
  def ytdStatistics(action: String, activity: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"demo ytd data for $action") { implicit request =>
      val activities = DemoActivityUtils.generate()
      val now = LocalDate.now()
      val series = toSumYtdSeries(activities, now, action, Units.Metric)
      Future(Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(series))))
    }
  }

  // demo yearly progress data
  // route mapped to /api/demo/statistics/yearly/:action/:activity
  def yearlyStatistics(action: String, activity: String): Action[AnyContent] = Action.async {
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
      case "distance"  => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(Units.Metric).distance))
      case "elevation" => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(Units.Metric).elevation))
      case other       => sys.error(s"not supported action: $other")
    }
    Future(Ok(Json.toJson(series)))
  }

  // word cloud titles and descriptions with occurrences
  // route mapped to /api/demo/wordcloud
  def wordcloud(): Action[AnyContent] = Action { implicit request =>
    val wcs = DemoActivityUtils
      .generateTitles(200)
      .map(_.trim.toLowerCase)
      .groupBy(identity)
      .view
      .mapValues(_.size)
      .map { case (k, v) => WordCloud(k, v) }
      .toList
      .filter(_.weight > 1)
      .sortBy(_.weight)
      .reverse

    Ok(JsonIo.write(wcs))
  }

  // route mapped to /api/demo/statistics/histogram/:action/:activity
  def yearlyHistogram(action: String, activity: String): Action[AnyContent] = Action { implicit request =>
    val activities = DemoActivityUtils.generate()
    val series = action.toLowerCase match {
      case "distance"  => apexcharts.toDistanceHeatmap(activities, activity, Units.Metric)
      case "elevation" => apexcharts.toElevationHeatmap(activities, Units.Metric)
      case other       => sys.error(s"not supported action: $other")
    }
    Ok(Json.toJson(series))
  }
}
