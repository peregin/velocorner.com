package model

import velocorner.api.{Progress, Units, chart}
import velocorner.api.chart.{DailyPoint, DailySeries}
import velocorner.model.YearlyProgress

object highcharts {

  def toDistanceSeries(items: Iterable[YearlyProgress], unit: Units.Entry): List[DailySeries] =
    toSeries(items, _.to(unit).distance)

  def toElevationSeries(items: Iterable[YearlyProgress], unit: Units.Entry): List[DailySeries] =
    toSeries(items, _.to(unit).elevation)

  def toTimeSeries(items: Iterable[YearlyProgress], unit: Units.Entry): List[DailySeries] =
    toSeries(items, _.to(unit).movingTime.toDouble / 3600) // regardless of the unit, calculate from seconds to hours

  private def toSeries(items: Iterable[YearlyProgress], fun: Progress => Double): List[DailySeries] =
    items
      .map(yp => chart.DailySeries(yp.year.toString, yp.progress.map(p => DailyPoint(p.day, fun(p.progress))).toList))
      .toList // must be a list because of the swagger spec generator
}
