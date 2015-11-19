package velocorner.model

import org.joda.time.{Days, LocalDate}

case class YearlyProgress(year: Int, progress: List[DailyProgress]) {

  def zeroOnMissingDate: YearlyProgress = {
    val day2Progress = progress.map(p => (p.day, p)).toMap
    val firstDate = new LocalDate(year, 1, 1)
    val lastDate = new LocalDate(year, 12, 31)
    val daysBetween = Days.daysBetween(firstDate, firstDate).getDays()
    val days = for (f <- 0 to daysBetween) yield firstDate.plusDays(f)
    val newProgress = days.map(d => day2Progress.getOrElse(d, DailyProgress(d, Progress.zero)))
    YearlyProgress(year, newProgress.toList)
  }
}

object YearlyProgress {

  def from(progress: List[DailyProgress]): List[YearlyProgress] = {
    // group by year
    val byYear = progress.groupBy(_.day.year().get())
    byYear.map { case (year, list) => YearlyProgress(year, list)}.toList.sortBy(_.year)
  }

  def aggregate(progress: List[YearlyProgress]): List[YearlyProgress] = {
    progress.map(yp => yp.copy(
      progress = yp.progress.scanLeft(DailyProgress(LocalDate.now, Progress.zero))((accu, i) => DailyProgress(i.day, accu.progress + i.progress)).tail )
    )
  }

  // maps empty dates with zero, looks better on the spline spline chart
  def zeroOnMissingDate(progress: List[YearlyProgress]): List[YearlyProgress] = {
    progress.map(_.zeroOnMissingDate)
  }
}
