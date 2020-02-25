package velocorner.api

import play.api.libs.json.{Format, Json}

object Achievements {
  implicit val format = Format[Achievements](Json.reads[Achievements], Json.writes[Achievements])
}

case class Achievements(
                         maxAverageSpeed: Option[Achievement],
                         maxDistance: Option[Achievement],
                         maxElevation: Option[Achievement],
                         maxAveragePower: Option[Achievement],
                         maxHeartRate: Option[Achievement],
                         maxAverageHeartRate: Option[Achievement],
                         minAverageTemperature: Option[Achievement],
                         maxAverageTemperature: Option[Achievement]
                       )