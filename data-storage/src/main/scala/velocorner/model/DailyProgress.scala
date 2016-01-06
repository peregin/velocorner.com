package velocorner.model

import org.joda.time.LocalDate


object DailyProgress {

  // key format: [athleteId, [2012,4,30]]
  def fromStorage(key: String, value: String) = {
    val rawDate = key.dropWhile((c) => c != ',').trim.stripPrefix(",").stripSuffix("]")
    val dateArray = rawDate.stripPrefix("[").stripSuffix("]").split(',').map(_.toInt)
    val day = LocalDate.parse(f"${dateArray(0)}%4d-${dateArray(1)}%02d-${dateArray(2)}%02d")
    DailyProgress(day, Progress.fromStorage(value))
  }
}


case class DailyProgress(day: LocalDate, progress: Progress) {

  def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
  def getDay = day.getDayOfMonth
}
