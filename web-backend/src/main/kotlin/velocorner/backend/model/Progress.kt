package velocorner.backend.model

const val MILES = 1.609344e3
const val FEET = 3.048006096e-1

/**
 * @param rides amount of rides in this aggregate
 * @param distance distance expressed in km.
 * @param longestDistance longest distance out of the given rides, expressed in km.
 * @param elevation elevation expressed in meters.
 * @param movingTime moving time expressed in seconds
 * @param averageSpeed expressed in kph.
 */
data class Progress(
    val days: Int,
    val rides: Int,
    val distance: Double,
    val longestDistance: Double,
    val movingTime: Long,
    val averageSpeed: Double,
    val elevation: Double,
    val longestElevation: Double
) {
    companion object {
        val zero = Progress(0, 0, 0.0, 0.0, 0, 0.0, 0.0, 0.0)
    }

    // append using operator overloading
    operator fun plus(that: Progress) = Progress(
        days = this.days + that.days,
        rides = this.rides + that.rides,
        distance = this.distance + that.distance,
        longestDistance = maxOf(this.longestDistance, that.longestDistance),
        movingTime = this.movingTime + that.movingTime,
        averageSpeed = maxOf(this.averageSpeed, that.averageSpeed),
        elevation = this.elevation + that.elevation,
        longestElevation = maxOf(this.longestElevation, that.longestElevation)
    )

    // multiply to calculate estimates using operator overloading
    operator fun times(f: Double) = Progress(
        days = factor(this.days, f),
        rides = factor(this.rides, f),
        distance = factor(this.distance, f),
        longestDistance = longestDistance,
        movingTime = factor(this.movingTime, f),
        averageSpeed = this.averageSpeed,
        elevation = factor(this.elevation, f),
        longestElevation = this.longestElevation
    )

    private fun <T> factor(v: T, f: Double): T {
        @Suppress("UNCHECKED_CAST")
        return when (v) {
            is Int -> (v * f).toInt() as T
            is Long -> (v * f).toLong() as T
            is Double -> (v * f) as T
            else -> throw IllegalArgumentException("Unsupported numeric type: ${v?.let { it::class.simpleName }}")
        }
    }


    fun to(unit: Units): Progress = when (unit) {
        Units.IMPERIAL -> toImperial()
        Units.METRIC -> this
    }

    private fun toImperial(): Progress = Progress(
        days = this.days,
        rides = this.rides,
        distance = distance * MILES,
        longestDistance = this.longestDistance * MILES,
        movingTime = this.movingTime,
        averageSpeed = this.averageSpeed * MILES / 3600,
        elevation = this.elevation * FEET,
        longestElevation = this.longestElevation * FEET
    )
}