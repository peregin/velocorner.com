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
    // everything
    log.info("TOTAL")
    printProgress(yearly)

    // every progress until current day
    val now = LocalDate.now()
    val cyclingActivitiesUntilThisDay = yearly.map(_.ytd(now))
    log.info(s"YEAR TO DATE $now")
    printProgress(cyclingActivitiesUntilThisDay)
  }

  protected def printProgress(byYear: Iterable[YearlyProgress]) {
    val aggregateByYear = byYear.map(YearlyAggregate.from)
    aggregateByYear.foreach(_.prettyPrint())
  }
}
