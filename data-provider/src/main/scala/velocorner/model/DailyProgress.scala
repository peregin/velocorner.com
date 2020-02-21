package velocorner.model

import org.joda.time.LocalDate
import velocorner.api.{Activity, Progress}


object DailyProgress {

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


case class DailyProgress(day: LocalDate, progress: Progress)