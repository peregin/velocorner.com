package velocorner.manual

import org.joda.time.LocalDate
import org.slf4s.Logging
import velocorner.model.{DailyProgress, YearlyProgress}

/**
 * Created by levi on 13/04/15.
 */
trait AggregateActivities extends Logging {

  def printAllProgress(cyclingActivities: Iterable[DailyProgress]) {
    printAll(YearlyProgress.from(cyclingActivities))
  }

  def printAll(yearly: Iterable[YearlyProgress]) {
    log.info("TOTAL")
    printProgress(yearly)

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

  protected def printProgress(byYear: Iterable[YearlyProgress]) {
    val aggregateByYear = byYear.map(YearlyAggregate.from)
    aggregateByYear.foreach(_.logSummary())
  }

}
