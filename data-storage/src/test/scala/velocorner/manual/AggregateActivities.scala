package velocorner.manual

import org.joda.time.LocalDate
import org.slf4s.Logging
import velocorner.model.{DailyProgress, YearlyProgress}

/**
 * Created by levi on 13/04/15.
 */
trait AggregateActivities extends Logging {

  def printAllProgress(cyclingActivities: Iterable[DailyProgress]) {
    log.info("TOTAL")
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

    log.info(s"YEAR TO DATE $now")
    printProgress(cyclingActivitiesUntilThisDay)
  }

  protected def printProgress(from: Iterable[DailyProgress]) {
    val byYear = YearlyProgress.from(from)
    val aggregateByYear = byYear.map(YearlyAggregate.from)
    aggregateByYear.foreach(_.logSummary())
  }

}
