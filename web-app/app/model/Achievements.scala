package model

import play.api.libs.json.{Format, Json}
import velocorner.model.Achievement

object Achievements {
  implicit val format = Format[Achievements](Json.reads[Achievements], Json.writes[Achievements])
}

case class Achievements(
                         maxSpeed: Option[Achievement],
                         maxAverageSpeed: Option[Achievement],
                         maxDistance: Option[Achievement],
                         maxElevation: Option[Achievement]
                       )