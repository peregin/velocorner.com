package velocorner.api

import play.api.libs.json._
import squants.Meters
import squants.motion.KilometersPerHour
import squants.space.Kilometers
import velocorner.api.Progress._

import scala.Numeric.Implicits._

object Progress {
  type FromDouble[T] = Double => T
  implicit object IntFromDouble extends FromDouble[Int] {
    override def apply(v: Double): Int = v.toInt
  }
  implicit object LongFromDouble extends FromDouble[Long] {
    override def apply(v: Double): Long = v.toLong
  }

  val zero = Progress(0, 0, 0d, 0d, 0, 0d, 0d, 0d)
  // implicit val monoidInstance = Monoid.instance[Progress](_ + _, zero)

  implicit val totalFormat: Format[Progress] = Format[Progress](Json.reads[Progress], Json.writes[Progress])
}

/**
 * @param rides amount of rides in this aggregate
 * @param distance distance expressed in km.
 * @param longestDistance longest distance out of the given rides, expressed in km.
 * @param elevation elevation expressed in meters.
 * @param movingTime moving time expressed in seconds
 * @param averageSpeed expressed in kph.
 */
case class Progress(
    days: Int,
    rides: Int,
    distance: Double,
    longestDistance: Double,
    movingTime: Long,
    averageSpeed: Double,
    elevation: Double,
    longestElevation: Double
) {

  // append
  def +(that: Progress) = Progress(
    days = this.days + that.days,
    rides = this.rides + that.rides,
    distance = this.distance + that.distance,
    longestDistance = this.longestDistance.max(that.longestDistance),
    movingTime = this.movingTime + that.movingTime,
    averageSpeed = this.averageSpeed.max(that.averageSpeed),
    elevation = this.elevation + that.elevation,
    longestElevation = this.longestElevation.max(that.longestElevation)
  )

  // multiply to calculate estimates
  def *(f: Double) = Progress(
    days = factor(this.days, f),
    rides = factor(this.rides, f),
    distance = factor(this.distance, f),
    longestDistance = longestDistance,
    movingTime = factor(this.movingTime, f),
    averageSpeed = this.averageSpeed,
    elevation = factor(this.elevation, f),
    longestElevation = this.longestElevation
  )

  def factor[T: Numeric: FromDouble](v: T, f: Double): T = v.toDouble * f

  def to(unit: Units.Entry): Progress = unit match {
    case Units.Imperial => this.toImperial()
    case Units.Metric   => this
    case other          => throw new IllegalArgumentException(s"unknown unit $other")
  }

  private def toImperial(): Progress = Progress(
    days = this.days,
    rides = this.rides,
    distance = Kilometers(this.distance).toInternationalMiles,
    longestDistance = Kilometers(this.longestDistance).toInternationalMiles,
    movingTime = this.movingTime,
    averageSpeed = KilometersPerHour(this.averageSpeed).toInternationalMilesPerHour,
    elevation = Meters(this.elevation).toFeet,
    longestElevation = Meters(this.longestElevation).toFeet
  )
}
