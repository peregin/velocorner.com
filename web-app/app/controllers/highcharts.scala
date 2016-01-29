import org.joda.time.LocalDate

/**
  * Created by levi on 29/01/16.
  */
package object highcharts {

  case class DailyPoint(day: LocalDate, value: Double)

  case class DailySeries(title: String, series: Iterable[DailyPoint])

}
