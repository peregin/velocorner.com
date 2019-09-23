package velocorner.model

import play.api.libs.json._

object Progress {

  implicit val totalFormat = Format[Progress](Json.reads[Progress], Json.writes[Progress])

  def zero = Progress(0, 0d, 0d, 0, 0d, 0d, 0d)
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

  // aggregate
  def +(that: Progress) = Progress(
    this.rides + that.rides,
    this.distance + that.distance,
    this.longestDistance.max(that.longestDistance),
    this.movingTime + that.movingTime,
    this.averageSpeed.max(that.averageSpeed),
    this.elevation + that.elevation,
    this.longestElevation.max(that.longestElevation)
  )
}
