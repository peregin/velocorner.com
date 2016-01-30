import org.joda.time.LocalDate
import velocorner.model.{Progress, YearlyProgress}

/**
  * Created by levi on 29/01/16.
  */
package object highcharts {

  case class DailyPoint(day: LocalDate, value: Double) {
    def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
    def getDay = day.getDayOfMonth
  }

  case class DailySeries(name: String, series: Iterable[DailyPoint])

  def toSeries(items: Iterable[YearlyProgress], fun: Progress => Double): Iterable[DailySeries] = {
    items.map(yp => DailySeries(yp.year.toString, yp.progress.map(p => DailyPoint(p.day, fun(p.progress)))))
  }

  def toDistanceSeries(items: Iterable[YearlyProgress]): Iterable[DailySeries] = toSeries(items, _.distance)

}
