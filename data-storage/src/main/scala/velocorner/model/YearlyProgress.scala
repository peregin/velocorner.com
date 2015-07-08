package velocorner.model


case class YearlyProgress(year: Int, progress: List[DailyProgress])

object YearlyProgress {

  def from(progress: List[DailyProgress]): List[YearlyProgress] = {
    // group by year
    val byYear = progress.groupBy(_.day.year().get())
    byYear.map { case (year, list) => YearlyProgress(year, list)}.toList.sortBy(_.year)
  }

//  def sum(progress: List[YearlyProgress]): List[YearlyProgress] = {
//    progress.map(yp => yp.copy(progress = yp.progress.scanLeft(Progress.zero)( (accu, i) => accu + i.progress))))
//  }
}