package model

import velocorner.api.Activity
import velocorner.api.heatmap.{HeatmapPoint, HeatmapSeries}

object apexcharts {

  def toDistanceHeatmap(items: Iterable[Activity]): List[HeatmapSeries] =
    toYearlyHeatmap(items, _.distance.toLong / 1000, List(
      HeatmapPoint("10km", 10),
      HeatmapPoint("50km", 50),
      HeatmapPoint("100km", 100),
      HeatmapPoint("150km", 150),
      HeatmapPoint("200km", 200)
    ))

  def toElevationHeatmap(items: Iterable[Activity]): List[HeatmapSeries] =
    toYearlyHeatmap(items, _.total_elevation_gain.toLong, List(
      HeatmapPoint("300m", 300),
      HeatmapPoint("600m", 600),
      HeatmapPoint("1000m", 1000),
      HeatmapPoint("1500m", 1500),
      HeatmapPoint("2000m", 2000)
    ))

  // must return a list because of the swagger spec generator
  private[model] def toYearlyHeatmap(items: Iterable[Activity], fun: Activity => Long, ranges: List[HeatmapPoint]): List[HeatmapSeries] = {
    val year2Values = items.groupMap(_.getStartDateLocal().year().get())(fun)
    toYearlyHeatmap(year2Values, ranges)
  }

  // returns with a sorted series, sorted by year and ranges
  private[model] def toYearlyHeatmap(year2Values: Map[Int, Iterable[Long]], ranges: List[HeatmapPoint]): List[HeatmapSeries] =
    year2Values.map{ case (year, sample) =>
      val biggest = ranges.last
      val namedPoints = sample.map(point => ranges.find(_.y > point).getOrElse(biggest).copy(y = point))
      val heatmapPoints = namedPoints.groupBy(_.x).view.mapValues(_.size).map{ case (name, count) => HeatmapPoint(name, count)}.toList
      // sort by the order given in the ranges
      val name2HeatmapPoints = heatmapPoints.groupBy(_.x)
      val sortedHeatmapPoints = ranges.flatMap(r => name2HeatmapPoints.get(r.x)).flatten
      HeatmapSeries(year.toString, sortedHeatmapPoints)
    }.toList.sortBy(_.name).reverse
}
