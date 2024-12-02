package velocorner.backend.model

import java.time.LocalDate

data class DailyPoint(val day: LocalDate, val value: Double)

data class DailySeries(val name: String, val series: List<DailyPoint>)

data class WordCloud(val name: String, val weight: Int)

data class HeatmapPoint(val x: String, val y: Long)

data class HeatmapSeries(val name: String, val data: List<HeatmapPoint>)

object Highcharts {
    fun toDistanceSeries(items: Iterable<YearlyProgress>, unit: Units): List<DailySeries> =
        toSeries(items) { it.to(unit).distance }

    fun toElevationSeries(items: Iterable<YearlyProgress>, unit: Units): List<DailySeries> =
        toSeries(items) { it.to(unit).elevation }

    fun toTimeSeries(items: Iterable<YearlyProgress>, unit: Units): List<DailySeries> =
        toSeries(items) { it.to(unit).movingTime.toDouble() / 3600 } // regardless of the unit, calculate from seconds to hours

    private fun toSeries(
        items: Iterable<YearlyProgress>,
        transform: (Progress) -> Double
    ): List<DailySeries> =
        items.map { yp ->
            DailySeries(
                yp.year.toString(),
                yp.progress.map { p ->
                    DailyPoint(p.day, transform(p.progress))
                }
            )
        }
}

object Apexcharts {
    val metricElevationRange = listOf(
        HeatmapPoint("0-300m", 300),
        HeatmapPoint("300-600m", 600),
        HeatmapPoint("600-1000m", 1000),
        HeatmapPoint("1000-1500m", 1500),
        HeatmapPoint("1500-2000m", 2000),
        HeatmapPoint("2000+ m", 3000)
    )

    val imperialElevationRange = listOf(
        HeatmapPoint("0-1000ft", 1000),
        HeatmapPoint("1000-1500ft", 1500),
        HeatmapPoint("1500-3000ft", 3000),
        HeatmapPoint("3000-4500ft", 4500),
        HeatmapPoint("4500-6000ft", 6000),
        HeatmapPoint("6000+ ft", 9000)
    )

    val metricRideDistanceRange = listOf(
        HeatmapPoint("0-10km", 10),
        HeatmapPoint("10-50km", 50),
        HeatmapPoint("50-100km", 100),
        HeatmapPoint("100-150km", 150),
        HeatmapPoint("150-200km", 200),
        HeatmapPoint("200+ km", 250)
    )

    val imperialRideDistanceRange = listOf(
        HeatmapPoint("0-5mi", 5),
        HeatmapPoint("5-20mi", 20),
        HeatmapPoint("20-50mi", 50),
        HeatmapPoint("50-80mi", 80),
        HeatmapPoint("80-100mi", 100),
        HeatmapPoint("100+ mi", 150)
    )

    val metricDistanceRange = listOf(
        HeatmapPoint("0-3km", 3),
        HeatmapPoint("3-5km", 5),
        HeatmapPoint("5-10km", 10),
        HeatmapPoint("10-15km", 15),
        HeatmapPoint("15+ km", 20)
    )

    val imperialDistanceRange = listOf(
        HeatmapPoint("0-2mi", 2),
        HeatmapPoint("2-5mi", 5),
        HeatmapPoint("5-8mi", 8),
        HeatmapPoint("8-10mi", 10),
        HeatmapPoint("10+ mi", 20)
    )

    fun toDistanceHeatmap(items: Sequence<Activity>, activityType: String, unit: Units): List<HeatmapSeries> {
        val (ranges, converter) = when {
            activityType == "Ride" && unit == Units.METRIC ->
                Pair(metricRideDistanceRange) { d: Long -> d }
            unit == Units.METRIC ->
                Pair(metricDistanceRange) { d: Long -> d }
            activityType == "Ride" && unit == Units.IMPERIAL ->
                Pair(imperialRideDistanceRange) { d: Long -> (d * MILES).toLong() }
            else ->
                Pair(imperialDistanceRange) { d: Long -> (d * MILES).toLong() }
        }
        return toYearlyHeatmap(items, { a -> converter(a.distance.toLong() / 1000) }, ranges)
    }

    fun toElevationHeatmap(items: Sequence<Activity>, unit: Units): List<HeatmapSeries> {
        val (ranges, converter) = when (unit) {
            Units.METRIC -> Pair(metricElevationRange) { d: Long -> d }
            else -> Pair(imperialElevationRange) { d: Long -> (d * FEET).toLong() }
        }
        return toYearlyHeatmap(items, { a -> converter(a.total_elevation_gain.toLong()) }, ranges)
    }

    internal fun toYearlyHeatmap(
        items: Sequence<Activity>,
        transform: (Activity) -> Long,
        ranges: List<HeatmapPoint>
    ): List<HeatmapSeries> {
        val year2Values = items.groupBy(
            { it.getStartDateLocal().year },
            { transform(it) }
        )
        return toYearlyHeatmap(year2Values, ranges)
    }

    internal fun toYearlyHeatmap(
        year2Values: Map<Int, List<Long>>,
        ranges: List<HeatmapPoint>
    ): List<HeatmapSeries> =
        year2Values
            .map { (year, sample) ->
                val biggest = ranges.last()
                val name2Count = sample
                    .map { point -> ranges.find { it.y > point }?.copy(y = point) ?: biggest.copy(y = point) }
                    .groupBy { it.x }
                    .mapValues { it.value.size }

                val heatmapPoints = ranges
                    .map { ref ->
                        name2Count[ref.x]?.let { count ->
                            HeatmapPoint(ref.x, count.toLong())
                        } ?: HeatmapPoint(ref.x, 0)
                    }
                    .reversed()

                HeatmapSeries(year.toString(), heatmapPoints)
            }
            .sortedByDescending { it.name }
}