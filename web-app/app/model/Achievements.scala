package model

import play.api.libs.json.{Format, Json}
import velocorner.model.Achievement

object Achievements {
  implicit val format = Format[Achievements](Json.reads[Achievements], Json.writes[Achievements])
}

case class Achievements(
                         maxAverageSpeed: Option[Achievement],
                         maxDistance: Option[Achievement],
                         maxElevation: Option[Achievement],
                         maxAveragePower: Option[Achievement],
                         maxHeartRate: Option[Achievement],
                         maxAverageHeartRate: Option[Achievement]
                       )