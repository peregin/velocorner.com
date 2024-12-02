package velocorner.backend.model

import java.time.Duration
import java.time.LocalDate

data class YearlyProgress(val year: Int, val progress: Iterable<DailyProgress>) {

    fun zeroOnMissingDate(): YearlyProgress {
        val day2Progress = progress.associate { it.day to it }
        val firstDate = LocalDate.of(year, 1, 1)
        val lastDate = LocalDate.of(year, 12, 31)
        val daysBetween = Duration.between(firstDate, lastDate).toDays()
        val days = (0..daysBetween).map { firstDate.plusDays(it) }
        val newProgress = days.map { d ->
            day2Progress.getOrElse(d) { DailyProgress(d, Progress.zero) }
        }
        return YearlyProgress(year, newProgress)
    }

    fun ytd(now: LocalDate): YearlyProgress {
        val monthToDate = now.monthValue
        val dayToDate = now.dayOfMonth
        return YearlyProgress(
            year,
            progress.filter { a ->
                val m = a.day.monthValue
                val d = a.day.dayOfMonth
                when {
                    m < monthToDate -> true
                    m == monthToDate -> d <= dayToDate
                    else -> false
                }
            }
        )
    }

    companion object {
        fun from(progress: Iterable<DailyProgress>): Iterable<YearlyProgress> {
            // group by year
            val byYear = progress.groupBy { it.day.year }
            return byYear.map { (year, list) ->
                YearlyProgress(year, list)
            }.sortedBy { it.year }
        }

        // each daily progress will be summed up with the previously aggregated progress
        fun aggregate(progress: Iterable<YearlyProgress>): Iterable<YearlyProgress> =
            progress.map { yp ->
                yp.copy(progress = DailyProgress.aggregate(yp.progress))
            }

        // maps empty dates with zero, looks better on the spline chart
        fun zeroOnMissingDate(progress: Iterable<YearlyProgress>): Iterable<YearlyProgress> =
            progress.map { it.zeroOnMissingDate() }

        // sum up ytd progress
        fun sumYtd(progress: Iterable<YearlyProgress>, until: LocalDate): Iterable<YearlyProgress> =
            progress
                .map { it.ytd(until) }
                .map { ytd ->
                    YearlyProgress(
                        ytd.year,
                        listOf(
                            DailyProgress(
                                LocalDate.parse("${ytd.year}-01-01"),
                                ytd.progress.map { it.progress }
                                    .fold(Progress.zero) { acc, curr -> acc + curr }
                            )
                        )
                    )
                }
    }
}