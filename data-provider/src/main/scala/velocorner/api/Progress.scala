package velocorner.api

import play.api.libs.json._
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

  val zero = Progress(0, 0d, 0d, 0, 0d, 0d, 0d)
  //implicit val monoidInstance = Monoid.instance[Progress](_ + _, zero)

  implicit val totalFormat = Format[Progress](Json.reads[Progress], Json.writes[Progress])
}

/**
 * @param rides amount of rides in this aggregate
 * @param distance distance expressed in km.
 * @param longestDistance longest distance out of the given rides, expressed in km.
 * @param elevation elevation expressed in meters.
 * @param movingTime moving time expressed in seconds
 * @param averageSpeed expressed in kph.
 */
case class Progress(rides: Int,
                    distance: Double, longestDistance: Double,
                    movingTime: Long,
                    averageSpeed: Double,
                    elevation: Double, longestElevation: Double) {

  // append
  def +(that: Progress) = Progress(
    this.rides + that.rides,
    this.distance + that.distance,
    this.longestDistance.max(that.longestDistance),
    this.movingTime + that.movingTime,
    this.averageSpeed.max(that.averageSpeed),
    this.elevation + that.elevation,
    this.longestElevation.max(that.longestElevation)
  )

  // multiply to calculate estimates
  def *(f: Double) = Progress(
    factor(this.rides, f),
    factor(this.distance, f),
    longestDistance,
    factor(this.movingTime, f),
    this.averageSpeed,
    factor(this.elevation, f),
    this.longestElevation
  )

  def factor[T : Numeric : FromDouble](v: T, f: Double): T = v.toDouble * f
}
