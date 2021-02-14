package model

import squants.space.{Kilometers, Meters}
import velocorner.api.heatmap.{HeatmapPoint, HeatmapSeries}
import velocorner.api.strava.Activity
import velocorner.model.Units

object apexcharts {

  val metricElevationRange = List(
    HeatmapPoint("300m", 300),
    HeatmapPoint("600m", 600),
    HeatmapPoint("1000m", 1000),
    HeatmapPoint("1500m", 1500),
    HeatmapPoint("2000m", 2000),
    HeatmapPoint("3000m", 3000)
  )
  val imperialElevationRange = List(
    HeatmapPoint("1000ft", 1000),
    HeatmapPoint("1500ft", 1500),
    HeatmapPoint("3000ft", 3000),
    HeatmapPoint("4500ft", 4500),
    HeatmapPoint("6000ft", 6000),
    HeatmapPoint("9000ft", 9000)
  )
  val metricRideDistanceRange = List(
    HeatmapPoint("10km", 10),
    HeatmapPoint("50km", 50),
    HeatmapPoint("100km", 100),
    HeatmapPoint("150km", 150),
    HeatmapPoint("200km", 200),
    HeatmapPoint("250km", 250)
  )
  val imperialRideDistanceRange = List(
    HeatmapPoint("5mi", 5),
    HeatmapPoint("20mi", 20),
    HeatmapPoint("50mi", 50),
    HeatmapPoint("80mi", 80),
    HeatmapPoint("100mi", 100),
    HeatmapPoint("150mi", 150)
  )
  val metricDistanceRange = List(
    HeatmapPoint("3km", 3),
    HeatmapPoint("5km", 5),
    HeatmapPoint("10km", 10),
    HeatmapPoint("15km", 15),
    HeatmapPoint("20km", 20)
  )
  val imperialDistanceRange = List(
    HeatmapPoint("2mi", 2),
    HeatmapPoint("5mi", 5),
    HeatmapPoint("8mi", 8),
    HeatmapPoint("10mi", 10),
    HeatmapPoint("20mi", 20)
  )

  def toDistanceHeatmap(items: Iterable[Activity], activityType: String, unit: Units.Entry): List[HeatmapSeries] = {
    val (ranges, converter) = (activityType, unit) match {
      case ("Ride", Units.Metric)   => (metricRideDistanceRange, identity[Long](_))
      case (_, Units.Metric)        => (metricDistanceRange, identity[Long](_))
      case ("Ride", Units.Imperial) => (imperialRideDistanceRange, (d: Long) => Kilometers(d).toInternationalMiles.toLong)
      case (_, Units.Imperial)      => (imperialDistanceRange, (d: Long) => Kilometers(d).toInternationalMiles.toLong)
    }
    toYearlyHeatmap(items, (a: Activity) => converter(a.distance.toLong / 1000), ranges)
  }

  def toElevationHeatmap(items: Iterable[Activity], unit: Units.Entry): List[HeatmapSeries] = {
    val (ranges, converter) = unit match {
      case Units.Metric => (metricElevationRange, identity[Long](_))
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
