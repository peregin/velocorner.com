package velocorner.model.weather

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.{DateTimePattern, EpochFormatter}
;

/**
  * The structure from the storage layer.
  */
object WeatherForecast {
  implicit val dateTimeFormat = DateTimePattern.createLongFormatter
  implicit val storageFormat = Format[WeatherForecast](Json.reads[WeatherForecast], Json.writes[WeatherForecast])
}

case class WeatherForecast
(
  location: String, // city[, country iso 2 letters]
  timestamp: DateTime,
  forecast: Weather
)
