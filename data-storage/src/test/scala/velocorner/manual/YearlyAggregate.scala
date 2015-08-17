package velocorner.manual

import org.slf4s.Logging
import velocorner.model.{YearlyProgress, Progress}

/**
 * Created by levi on 17/08/15.
 */
object YearlyAggregate {

  def from(yp: YearlyProgress): YearlyAggregate = {
    val aggregatedProgresss = yp.progress.foldLeft(Progress.zero)((accu, dailyProgress) => accu + dailyProgress.progress)
    YearlyAggregate(yp.year, aggregatedProgresss)
  }
}

case class YearlyAggregate(year: Int, aggregate: Progress) extends Logging {

  def logSummary() {
    log.info(f"year $year -> ${aggregate.distance}%.2f km, \u2191 ${aggregate.elevation}%.0f m ")
  }
}
