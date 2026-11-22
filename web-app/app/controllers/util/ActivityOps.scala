package controllers.util

import model.ActivitySeries
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
      case "distance"  => ActivitySeries.toDistanceSeries(ytdSum, unit)
      case "elevation" => ActivitySeries.toElevationSeries(ytdSum, unit)
      case "time"      => ActivitySeries.toTimeSeries(ytdSum, unit)
      case other       => sys.error(s"not supported action: $other")
    }
  }

  def toYearlySeries(activities: Iterable[Activity], action: String, unit: Units.Entry): List[DailySeries] = {
    val dailyProgress = DailyProgress.from(activities)
    val yearlyProgress = YearlyProgress.from(dailyProgress)
    action.toLowerCase match {
      case "heatmap"   => ActivitySeries.toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress), unit)
      case "distance"  => ActivitySeries.toDistanceSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case "elevation" => ActivitySeries.toElevationSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case "time"      => ActivitySeries.toTimeSeries(YearlyProgress.aggregate(yearlyProgress), unit)
      case other       => sys.error(s"not supported action: $other")
    }
  }
}
