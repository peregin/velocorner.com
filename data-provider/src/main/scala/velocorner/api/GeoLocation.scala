package velocorner.api

import play.api.libs.json.{Format, Json}

object GeoLocation {
  implicit val glFormat = Format[GeoLocation](Json.reads[GeoLocation], Json.writes[GeoLocation])
}

// country is represented in ISO code 2 (CH, HU, etc.)
case class GeoLocation(city: String, country: String)
