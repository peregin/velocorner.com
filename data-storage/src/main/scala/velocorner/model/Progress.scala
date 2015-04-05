package velocorner.model

import org.joda.time.LocalDate
import play.api.libs.json._

object Progress {

  /**
   * key format: [2012,4,30]
   * value format: {"ride":1,"dist":6741.7998046875,"distmax":6741.7998046875,"elev":93.0999984741211,"elevmax":93.0999984741211,"time":1144}
   * Note that distance is mapped to meters and time is mapped to seconds
   */
  def fromStorage(key: Option[String], value: String): Progress = {
    val day = key.map(_.stripPrefix("[").stripSuffix("]").split(',').map(_.toInt)).map(v => f"${v(0)}%4d-${v(1)}%02d-${v(2)}%02d").map(LocalDate.parse)
    val jspath = Json.parse(value)
    val rides = (jspath \ "ride").as[Int]
    val distance = (jspath \ "dist").as[Double] / 1000
    val longestDistance = (jspath \ "distmax").as[Double] / 1000
    val movingTime = (jspath \ "time").as[Long]
    val averageSpeed = if (movingTime != 0) distance * 3600 / movingTime else 0
    val elevation = (jspath \ "elev").as[Double]
    val longestElevation = (jspath \ "elevmax").as[Double]
    Progress(day, rides, distance, longestDistance, movingTime, averageSpeed, elevation, longestElevation)
  }
}

/**
 * @param day if is a daily progress then specifies the given date, otherwise is not set.
 * @param rides amount of rides in this aggregate
 * @param distance distance expressed in km.
 * @param longestDistance longest distance out of the given rides, expressed in km.
 * @param elevation elevation expressed in meters.
 * @param movingTime moving time expressed in seconds
 * @param averageSpeed expressed in kph.
 */
case class Progress(day: Option[LocalDate],
                    rides: Int,
                    distance: Double, longestDistance: Double, movingTime: Long,
                    averageSpeed: Double,
                    elevation: Double, longestElevation: Double)
