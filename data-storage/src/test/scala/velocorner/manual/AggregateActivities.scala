package velocorner.manual

import org.joda.time.LocalDate
import org.slf4s.Logging
import velocorner.model.{DailyProgress, Activity}

/**
 * Created by levi on 13/04/15.
 */
trait AggregateActivities extends Logging {

  def printAll(cyclingActivities: List[Activity]) {
    log.info("Total")
    print(cyclingActivities)

    // each until current day
    val now = LocalDate.now()
    val mn = now.monthOfYear().get()
    val dn = now.getDayOfMonth
    val cyclingActivitiesUntilThisDay = cyclingActivities.filter{a =>
      val m = a.start_date_local.monthOfYear().get()
      val d = a.start_date_local.dayOfMonth().get()
      if (m < mn) true
      else if (m == mn) d <= dn
      else false
    }

    log.info("Until this day")
    print(cyclingActivitiesUntilThisDay)
  }

  def printAllProgress(cyclingActivities: List[DailyProgress]) {
    log.info("Total")
    printProgress(cyclingActivities)

    // each until current day
    val now = LocalDate.now()
    val mn = now.monthOfYear().get()
    val dn = now.getDayOfMonth
    val cyclingActivitiesUntilThisDay = cyclingActivities.filter{a =>
      val m = a.day.monthOfYear().get()
      val d = a.day.dayOfMonth().get()
      if (m < mn) true
      else if (m == mn) d <= dn
      else false
    }

    log.info("Until this day")
    printProgress(cyclingActivitiesUntilThisDay)
  }


  protected def print(from: List[Activity]) {
    // group by year
    val byYear = from.groupBy(_.start_date_local.year().get())
    // total km in each year
    val yearWithDistance = byYear.map { case (year, list) => (year, list.map(_.distance).sum / 1000) }.toList.sortBy(_._1)
    yearWithDistance.foreach(e => log.info(f"year ${e._1} -> ${e._2}%.2f km"))
  }

  protected def printProgress(from: List[DailyProgress]) {
    // group by year
    val byYear = from.groupBy(_.day.year().get())
    // total km in each year
    val yearWithDistance = byYear.map { case (year, list) => (year, list.map(_.progress.distance).sum) }.toList.sortBy(_._1)
    yearWithDistance.foreach(e => log.info(f"year ${e._1} -> ${e._2}%.2f km"))
  }

}
