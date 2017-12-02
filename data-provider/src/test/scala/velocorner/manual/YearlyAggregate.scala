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

  def prettyPrint() {
    log.info(f"year $year -> ${aggregate.distance}%6.0f km, ${aggregate.elevation}%7.0f \u2191m, ${aggregate.rides}%4d rides")
  }
}
