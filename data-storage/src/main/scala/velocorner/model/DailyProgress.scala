package velocorner.model

import org.joda.time.LocalDate


object DailyProgress {

  // key format: [2012,4,30]
  def fromStorage(key: String, value: String) = {
    val date = key.stripPrefix("[").stripSuffix("]").split(',').map(_.toInt)
    val day = LocalDate.parse(f"${date(0)}%4d-${date(1)}%02d-${date(2)}%02d")
    DailyProgress(day, Progress.fromStorage(value))
  }
}


case class DailyProgress(day: LocalDate, progress: Progress)
