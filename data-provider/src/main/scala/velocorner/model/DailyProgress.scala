package velocorner.model

import org.joda.time.LocalDate
import velocorner.model.strava.Activity


object DailyProgress {

  // key format: [2012,4,30]
  def parseDate(text: String): LocalDate = {
    val dateArray = text.stripPrefix("[").stripSuffix("]").split(',').map(_.toInt)
    LocalDate.parse(f"${dateArray(0)}%4d-${dateArray(1)}%02d-${dateArray(2)}%02d")
  }

  def fromStorage(activity: Activity): DailyProgress = {
    val progress = new Progress(1,
      activity.distance / 1000, activity.distance / 1000, activity.moving_time,
      activity.average_speed.getOrElse(0f).toDouble,
      activity.total_elevation_gain, activity.total_elevation_gain
    )
    DailyProgress(activity.start_date_local.toLocalDate, progress)
  }

  def fromStorage(activities: Iterable[Activity]): Iterable[DailyProgress] = {
    activities.map(fromStorage)
      .groupBy(_.day)
      .map{ case (day, progressPerDay) =>
        DailyProgress(day, progressPerDay.foldLeft(Progress.zero)((accu, dailyProgress) => accu + dailyProgress.progress))
      }.toSeq.sortBy(_.day.toString)
  }

  def aggregate(list: Iterable[DailyProgress]): Iterable[DailyProgress] = {
    list.scanLeft(DailyProgress(LocalDate.now, Progress.zero))((accu, i) =>
      DailyProgress(i.day, accu.progress + i.progress)).tail
  }
}


case class DailyProgress(day: LocalDate, progress: Progress) {

  def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
  def getDay = day.getDayOfMonth
}