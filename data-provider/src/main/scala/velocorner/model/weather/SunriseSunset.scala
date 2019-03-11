package velocorner.model.weather

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.EpochFormatter
;

/**
  * The structure from the storage layer.
  */
object SunriseSunset {
  implicit val dateTimeFormat = EpochFormatter.create
  implicit val storageFormat = Format[SunriseSunset](Json.reads[SunriseSunset], Json.writes[SunriseSunset])
}

case class SunriseSunset
(
  location: String, // city[, country iso 2 letters]
  timestamp: Long,
  sunrise: DateTime,
  sunset: DateTime
)
