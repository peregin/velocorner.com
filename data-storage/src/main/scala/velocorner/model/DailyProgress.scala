package velocorner.model

import org.joda.time.LocalDate


object DailyProgress {

  // key format: [athleteId, [2012,4,30]]
  def fromStorageByIdDay(key: String, value: String) = {
    val rawDate = key.dropWhile((c) => c != ',').trim.stripPrefix(",").stripSuffix("]")
    val day = parseDate(rawDate)
    DailyProgress(day, Progress.fromStorage(value))
  }

  // key format: [2012,4,30]
  def parseDate(text: String): LocalDate = {
    val dateArray = text.stripPrefix("[").stripSuffix("]").split(',').map(_.toInt)
    LocalDate.parse(f"${dateArray(0)}%4d-${dateArray(1)}%02d-${dateArray(2)}%02d")
  }
}


case class DailyProgress(day: LocalDate, progress: Progress) {

  def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
  def getDay = day.getDayOfMonth
}


object AthleteDailyProgress {

  // key format: [[2012,4,30], athleteId]
  // [[2016,1,25],432909]
  def fromStorageByDateId(key: String, value: String) = {
    val ix = key.lastIndexOf(',')
    val athleteId = key.substring(ix+1).stripSuffix("]").trim.toInt
    val rawDate = key.take(ix).stripPrefix("[").trim
    val day = DailyProgress.parseDate(rawDate)
    AthleteDailyProgress(athleteId, DailyProgress(day, Progress.fromStorage(value)))
  }

  // keep only for the current year
  def keepMostRecentDays(list: Iterable[AthleteDailyProgress], days: Int): Iterable[AthleteDailyProgress] = {
    if (list.isEmpty) list
    else {
      implicit val cmp = new Ordering[LocalDate] {
        override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
      }
      val mostRecentDay = list.map(_.dailyProgress.day).max
      val lastDayToKeep = mostRecentDay.minusDays(days)
      list.filter(_.dailyProgress.day.isAfter(lastDayToKeep)).filter(_.dailyProgress.day.getYear == mostRecentDay.getYear)
    }
  }
}

case class AthleteDailyProgress(athleteId: Int, dailyProgress: DailyProgress)