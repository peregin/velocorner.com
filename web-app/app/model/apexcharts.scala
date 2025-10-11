package model

import squants.space.{Kilometers, Meters}
import velocorner.api.Units
import velocorner.api.heatmap.{HeatmapPoint, HeatmapSeries}
import velocorner.api.strava.Activity

object apexcharts {

  val metricElevationRange = List(
    HeatmapPoint("0-300m", 300),
    HeatmapPoint("300-600m", 600),
    HeatmapPoint("600-1000m", 1000),
    HeatmapPoint("1000-1500m", 1500),
    HeatmapPoint("1500-2000m", 2000),
    HeatmapPoint("2000+ m", 3000)
  )
  val imperialElevationRange = List(
    HeatmapPoint("0-1000ft", 1000),
    HeatmapPoint("1000-1500ft", 1500),
    HeatmapPoint("1500-3000ft", 3000),
    HeatmapPoint("3000-4500ft", 4500),
    HeatmapPoint("4500-6000ft", 6000),
    HeatmapPoint("6000+ ft", 9000)
  )
  val metricRideDistanceRange = List(
    HeatmapPoint("0-10km", 10),
    HeatmapPoint("10-50km", 50),
    HeatmapPoint("50-100km", 100),
    HeatmapPoint("100-150km", 150),
    HeatmapPoint("150-200km", 200),
    HeatmapPoint("200+ km", 250)
  )
  val imperialRideDistanceRange = List(
    HeatmapPoint("0-5mi", 5),
    HeatmapPoint("5-20mi", 20),
    HeatmapPoint("20-50mi", 50),
    HeatmapPoint("50-80mi", 80),
    HeatmapPoint("80-100mi", 100),
    HeatmapPoint("100+ mi", 150)
  )
  val metricDistanceRange = List(
    HeatmapPoint("0-3km", 3),
    HeatmapPoint("3-5km", 5),
    HeatmapPoint("5-10km", 10),
    HeatmapPoint("10-15km", 15),
    HeatmapPoint("15+ km", 20)
  )
  val imperialDistanceRange = List(
    HeatmapPoint("0-2mi", 2),
    HeatmapPoint("2-5mi", 5),
    HeatmapPoint("5-8mi", 8),
    HeatmapPoint("8-10mi", 10),
    HeatmapPoint("10+ mi", 20)
  )

  def toDistanceHeatmap(items: Iterable[Activity], activityType: String, unit: Units.Entry): List[HeatmapSeries] = {
    val (ranges, converter) = (activityType, unit) match {
      case ("Ride", Units.Metric)   => (metricRideDistanceRange, identity[Long] _)
      case (_, Units.Metric)        => (metricDistanceRange, identity[Long] _)
      case ("Ride", Units.Imperial) => (imperialRideDistanceRange, (d: Long) => Kilometers(d).toInternationalMiles.toLong)
      case (_, Units.Imperial)      => (imperialDistanceRange, (d: Long) => Kilometers(d).toInternationalMiles.toLong)
    }
    toYearlyHeatmap(items, (a: Activity) => converter(a.distance.toLong / 1000), ranges)
  }

  def toElevationHeatmap(items: Iterable[Activity], unit: Units.Entry): List[HeatmapSeries] = {
    val (ranges, converter) = unit match {
      case Units.Metric => (metricElevationRange, identity[Long] _)
      case _            => (imperialElevationRange, (d: Long) => Meters(d).toFeet.toLong)
    }
    toYearlyHeatmap(items, (a: Activity) => converter(a.total_elevation_gain.toLong), ranges)
  }

  // must return a list because of the swagger spec generator
  private[model] def toYearlyHeatmap(items: Iterable[Activity], fun: Activity => Long, ranges: List[HeatmapPoint]): List[HeatmapSeries] = {
    val year2Values = items.groupMap(_.getStartDateLocal().year().get())(fun)
    toYearlyHeatmap(year2Values, ranges)
  }

  // returns with a sorted series, sorted by year and ranges
  private[model] def toYearlyHeatmap(year2Values: Map[Int, Iterable[Long]], ranges: List[HeatmapPoint]): List[HeatmapSeries] =
    year2Values
      .map { case (year, sample) =>
        val biggest = ranges.last
        val name2Count = sample
          .map(point => ranges.find(_.y > point).getOrElse(biggest).copy(y = point))
          .groupBy(_.x)
          .view
          .mapValues(_.size)
        // collect in the order given in the ranges and fill missing buckets with zero
        val heatmapPoints = ranges
          .foldRight(List.empty[HeatmapPoint])((ref: HeatmapPoint, accu: List[HeatmapPoint]) =>
            accu :+ name2Count
              .get(ref.x)
              .map(HeatmapPoint(ref.x, _))
              .getOrElse(HeatmapPoint(ref.x, 0))
          )
          .reverse
        HeatmapSeries(year.toString, heatmapPoints)
      }
      .toList
      .sortBy(_.name)
      .reverse
}
