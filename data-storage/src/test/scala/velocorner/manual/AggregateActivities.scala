package velocorner.manual

import org.joda.time.LocalDate
import org.slf4s.Logging
import velocorner.model.{YearlyProgress, DailyProgress}

/**
 * Created by levi on 13/04/15.
 */
trait AggregateActivities extends Logging {

  def printAllProgress(cyclingActivities: List[DailyProgress]) {
    log.info("Total")
    printProgress(cyclingActivities)

    // each until current day
    val now = LocalDate.now()
    val mn = now.monthOfYear().get()
    val dn = now.getDayOfMonth
    val cyclingActivitiesUntilThisDay = cyclingActivities.filter{ a =>
      val m = a.day.monthOfYear().get()
      val d = a.day.dayOfMonth().get()
      if (m < mn) true
      else if (m == mn) d <= dn
      else false
    }

    log.info("Until this day")
    printProgress(cyclingActivitiesUntilThisDay)
  }

  protected def printProgress(from: List[DailyProgress]) {
    val byYear = YearlyProgress.from(from)
    case class YearlyDistance(year: Int, distance: Double)
    val yearWithDistance = byYear.map(yp => YearlyDistance(yp.year, yp.progress.map(_.progress.distance).sum)).sortBy(_.year)
    yearWithDistance.foreach(e => log.info(f"year ${e.year} -> ${e.distance}%.2f km"))
  }

}
