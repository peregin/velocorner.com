package controllers.util

import model.highcharts
import org.joda.time.LocalDate
import velocorner.api.Units
import velocorner.api.chart.DailySeries
import velocorner.api.strava.Activity
import velocorner.model.{DailyProgress, YearlyProgress}

trait ActivityOps {

  def toSumYtdSeries(activities: Iterable[Activity], now: LocalDate, action: String, unit: Units.Entry): List[DailySeries] = {
    val dailyProgress = DailyProgress.from(activities)
    val yearlyProgress = YearlyProgress.from(dailyProgress)
    val ytdSum = YearlyProgress.sumYtd(yearlyProgress, now)
    action.toLowerCase match {
      case "distance"  => highcharts.toDistanceSeries(ytdSum, unit)
      case "elevation" => highcharts.toElevationSeries(ytdSum, unit)
      case "time"      => highcharts.toTimeSeries(ytdSum, unit)
      case other       => sys.error(s"not supported action: $other")
    }
  }

  def toYearlySeries(activities: Iterable[Activity], action: String, unit: Units.Entry): List[DailySeries] = {
    val dailyProgress = DailyProgress.from(activities)
    val yearlyProgress = YearlyProgress.from(dailyProgress)
    action.toLowerCase match {
      case "heatmap"   => highcharts.toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress), unit)
      case "distance"  => highcharts.toDistanceSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case "elevation" => highcharts.toElevationSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case "time"      => highcharts.toTimeSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case other       => sys.error(s"not supported action: $other")
    }
  }
}
