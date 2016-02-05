import org.joda.time.LocalDate
import velocorner.model.{AthleteDailyProgress, Progress, YearlyProgress}

/**
  * Created by levi on 29/01/16.
  */
package object highcharts {

  case class DailyPoint(day: LocalDate, value: Double) {
    def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
    def getDay = day.getDayOfMonth
  }

  case class DailySeries(name: String, series: Iterable[DailyPoint]) {

    def aggregate() = {
      val aggregatedSeries = series.toSeq.reverse.scanLeft(DailyPoint(LocalDate.now(), 0))((accu, i) =>
        DailyPoint(i.day, accu.value + i.value)).tail
      DailySeries(name, aggregatedSeries)
    }
  }


  def toDistanceSeries(items: Iterable[YearlyProgress]) = toSeries(items, _.distance)

  private def toSeries(items: Iterable[YearlyProgress], fun: Progress => Double): Iterable[DailySeries] = {
    items.map(yp => DailySeries(yp.year.toString, yp.progress.map(p => DailyPoint(p.day, fun(p.progress)))))
  }


  def toAthleteDistanceSeries(items: Iterable[AthleteDailyProgress]) = toAthleteSeries(items, _.distance)

  def toAthleteElevationSeries(items: Iterable[AthleteDailyProgress]) = toAthleteSeries(items, _.elevation)

  private def toAthleteSeries(items: Iterable[AthleteDailyProgress], fun: Progress => Double): Iterable[DailySeries] = {
    items.groupBy(_.athleteId).map{case (athleteId, list) =>
      DailySeries(athleteId.toString, list.map(e => DailyPoint(e.dailyProgress.day, fun(e.dailyProgress.progress))))
    }
  }
}
