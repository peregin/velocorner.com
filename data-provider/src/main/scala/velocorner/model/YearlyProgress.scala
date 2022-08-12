package velocorner.model

import org.joda.time.{Days, LocalDate}
import velocorner.api.Progress

case class YearlyProgress(year: Int, progress: Iterable[DailyProgress]) {

  def zeroOnMissingDate: YearlyProgress = {
    val day2Progress = progress.map(p => (p.day, p)).toMap
    val firstDate = new LocalDate(year, 1, 1)
    val lastDate = new LocalDate(year, 12, 31)
    val daysBetween = Days.daysBetween(firstDate, lastDate).getDays
    val days = for (f <- 0 to daysBetween) yield firstDate.plusDays(f)
    val newProgress = days.map(d => day2Progress.getOrElse(d, DailyProgress(d, Progress.zero)))
    YearlyProgress(year, newProgress)
  }

  def ytd(now: LocalDate): YearlyProgress = {
    val monthToDate = now.monthOfYear().get()
    val dayToDate = now.getDayOfMonth
    YearlyProgress(
      year,
      progress.filter { a =>
        val m = a.day.monthOfYear().get()
        val d = a.day.dayOfMonth().get()
        if (m < monthToDate) true
        else if (m == monthToDate) d <= dayToDate
        else false
      }
    )
  }
}

object YearlyProgress {

  def from(progress: Iterable[DailyProgress]): Iterable[YearlyProgress] = {
    // group by year
    val byYear = progress.groupBy(_.day.year().get())
    byYear.map { case (year, list) => YearlyProgress(year, list) }.toList.sortBy(_.year)
  }

  // each daily progress will be summed up with the previously aggregated progress
  def aggregate(progress: Iterable[YearlyProgress]): Iterable[YearlyProgress] =
    progress.map(yp => yp.copy(progress = DailyProgress.aggregate(yp.progress)))

  // maps empty dates with zero, looks better on the spline spline chart
  def zeroOnMissingDate(progress: Iterable[YearlyProgress]): Iterable[YearlyProgress] =
    progress.map(_.zeroOnMissingDate)

  // sum up ytd progress
  def sumYtd(progress: Iterable[YearlyProgress], until: LocalDate): Iterable[YearlyProgress] = progress
    .map(_.ytd(until))
    .map(ytd =>
      YearlyProgress(
        ytd.year,
        Seq(
          DailyProgress(
            LocalDate.parse(s"${ytd.year}-01-01"),
            ytd.progress.map(_.progress).foldLeft(Progress.zero)(_ + _)
          )
        )
      )
    )
}
