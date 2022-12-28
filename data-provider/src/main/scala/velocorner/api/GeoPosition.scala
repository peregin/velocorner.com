package velocorner.api

import play.api.libs.json.{Format, Json}

object GeoPosition {
  implicit val gpFormat = Format[GeoPosition](Json.reads[GeoPosition], Json.writes[GeoPosition])
}

// windy widget needs geo location as input
case class GeoPosition(latitude: Double, longitude: Double)
