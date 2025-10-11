package velocorner.api

import play.api.libs.json.{Format, Json}
import squants.motion.KilometersPerHour
import squants.space.{Kilometers, Meters}
import squants.thermal.Celsius

object Achievements {
  implicit val format: Format[Achievements] = Format[Achievements](Json.reads[Achievements], Json.writes[Achievements])
}

case class Achievements(
    maxAverageSpeed: Option[Achievement],
    maxDistance: Option[Achievement],
    maxTimeInSec: Option[Achievement],
    maxElevation: Option[Achievement],
    maxAveragePower: Option[Achievement],
    maxHeartRate: Option[Achievement],
    maxAverageHeartRate: Option[Achievement],
    minAverageTemperature: Option[Achievement],
    maxAverageTemperature: Option[Achievement]
) {

  def to(unit: Units.Entry): Achievements = unit match {
    case Units.Imperial => this.toImperial()
    case Units.Metric   => this
    case other          => throw new IllegalArgumentException(s"unknown unit $other")
  }

  private def toImperial() = Achievements(
    maxAverageSpeed = maxAverageSpeed.map(_.convert(KilometersPerHour(_).toInternationalMilesPerHour)),
    maxDistance = maxDistance.map(_.convert(Kilometers(_).toInternationalMiles)),
    maxTimeInSec = maxTimeInSec,
    maxElevation = maxElevation.map(_.convert(Meters(_).toFeet)),
    maxAveragePower = maxAveragePower,
    maxHeartRate = maxHeartRate,
    maxAverageHeartRate = maxAverageHeartRate,
    minAverageTemperature = minAverageTemperature.map(_.convert(Celsius(_).inFahrenheit.value)),
    maxAverageTemperature = maxAverageTemperature.map(_.convert(Celsius(_).inFahrenheit.value))
  )
}
