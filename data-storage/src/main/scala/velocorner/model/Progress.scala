package velocorner.model

import play.api.libs.json._

object Progress {

  /**
   * value format: {"ride":1,"dist":6741.7998046875,"distmax":6741.7998046875,"elev":93.0999984741211,"elevmax":93.0999984741211,"time":1144}
   * Note that distance is mapped to meters and time is mapped to seconds
   */
  def fromStorage(value: String): Progress = {
    val jsPath = Json.parse(value)
    val rides = (jsPath \ "ride").as[Int]
    val distance = (jsPath \ "dist").as[Double] / 1000
    val longestDistance = (jsPath \ "distmax").as[Double] / 1000
    val movingTime = (jsPath \ "time").as[Long]
    val averageSpeed = if (movingTime != 0) distance * 3600 / movingTime else 0
    val elevation = (jsPath \ "elev").as[Double]
    val longestElevation = (jsPath \ "elevmax").as[Double]
    Progress(rides, distance, longestDistance, movingTime, averageSpeed, elevation, longestElevation)
  }

  def from(activity: Activity): DailyProgress = {
    val progress = new Progress(1,
      activity.distance, activity.distance, activity.moving_time,
      activity.average_speed.getOrElse(0f).toDouble,
      activity.total_elevation_gain, activity.total_elevation_gain
    )
    DailyProgress(activity.start_date_local.toLocalDate, progress)
  }

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
                    distance: Double, longestDistance: Double, movingTime: Long,
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
