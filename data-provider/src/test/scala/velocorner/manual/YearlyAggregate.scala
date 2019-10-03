package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.model.{Progress, YearlyProgress}

/**
 * Created by levi on 17/08/15.
 */
object YearlyAggregate {

  def from(yp: YearlyProgress): YearlyAggregate = {
    val aggregatedProgresss = yp.progress.foldLeft(Progress.zero)((accu, dailyProgress) => accu + dailyProgress.progress)
    YearlyAggregate(yp.year, aggregatedProgresss)
  }
}

case class YearlyAggregate(year: Int, aggregate: Progress) extends LazyLogging {

  def prettyPrint(): Unit = {
    logger.info(f"year $year -> ${aggregate.distance}%6.0f km, ${aggregate.elevation}%7.0f \u2191m, ${aggregate.rides}%4d rides")
  }
}
