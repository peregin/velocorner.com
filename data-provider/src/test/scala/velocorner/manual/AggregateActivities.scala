package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import org.joda.time.LocalDate
import velocorner.model.{DailyProgress, YearlyProgress}

/**
 * Created by levi on 13/04/15.
 */
trait AggregateActivities extends LazyLogging {

  def printAllProgress(cyclingActivities: Iterable[DailyProgress]) {
    printAll(YearlyProgress.from(cyclingActivities))
  }

  def printAll(yearly: Iterable[YearlyProgress]) {
    // everything
    logger.info("TOTAL")
    printProgress(yearly)

    // every progress until current day
    val now = LocalDate.now()
    val cyclingActivitiesUntilThisDay = yearly.map(_.ytd(now))
    logger.info(s"YEAR TO DATE $now")
    printProgress(cyclingActivitiesUntilThisDay)
  }

  protected def printProgress(byYear: Iterable[YearlyProgress]) {
    val aggregateByYear = byYear.map(YearlyAggregate.from)
    aggregateByYear.foreach(_.prettyPrint())
  }
}
