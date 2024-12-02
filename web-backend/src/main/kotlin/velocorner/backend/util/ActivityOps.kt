package velocorner.backend.util

import velocorner.backend.model.*
import java.time.LocalDate

object ActivityOps {

    fun toSumYtdSeries(
        activities: Sequence<Activity>,
        now: LocalDate,
        action: String,
        unit: Units
    ): List<DailySeries> {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)
        val ytdSum = YearlyProgress.sumYtd(yearlyProgress, now)

        return when (action.lowercase()) {
            "distance" -> Highcharts.toDistanceSeries(ytdSum, unit)
            "elevation" -> Highcharts.toElevationSeries(ytdSum, unit)
            "time" -> Highcharts.toTimeSeries(ytdSum, unit)
            else -> error("not supported action: $action")
        }
    }

    fun toYearlySeries(
        activities: Sequence<Activity>,
        action: String,
        unit: Units
    ): List<DailySeries> {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)

        return when (action.lowercase()) {
            "heatmap" -> Highcharts.toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress), unit)
            "distance" -> Highcharts.toDistanceSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            "elevation" -> Highcharts.toElevationSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            "time" -> Highcharts.toTimeSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            else -> error("not supported action: $action")
        }
    }
}