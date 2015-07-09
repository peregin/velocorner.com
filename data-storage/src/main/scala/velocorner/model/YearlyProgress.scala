package velocorner.model

import org.joda.time.LocalDate

case class YearlyProgress(year: Int, progress: List[DailyProgress])

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
}
