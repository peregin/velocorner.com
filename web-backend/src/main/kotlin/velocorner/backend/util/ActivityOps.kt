package velocorner.backend.util

import velocorner.backend.model.Activity
import velocorner.backend.model.Units

object ActivityOps {

    fun toSumYtdSeries(
        activities: Iterable<Activity>,
        now: LocalDate,
        action: String,
        unit: Units
    ): List<DailySeries> {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)
        val ytdSum = YearlyProgress.sumYtd(yearlyProgress, now)

        return when (action.lowercase()) {
            "distance" -> highcharts.toDistanceSeries(ytdSum, unit)
            "elevation" -> highcharts.toElevationSeries(ytdSum, unit)
            "time" -> highcharts.toTimeSeries(ytdSum, unit)
            else -> error("not supported action: $action")
        }
    }

    fun toYearlySeries(
        activities: Iterable<Activity>,
        action: String,
        unit: Units
    ): List<DailySeries> {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)

        return when (action.lowercase()) {
            "heatmap" -> highcharts.toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress), unit)
            "distance" -> highcharts.toDistanceSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            "elevation" -> highcharts.toElevationSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            "time" -> highcharts.toTimeSeries(YearlyProgress.aggregate(yearlyProgress), unit)
            else -> error("not supported action: $action")
        }
    }
}