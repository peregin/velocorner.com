package velocorner.api.weather

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

// TODO: cleanup
/**
 * The structure from the storage layer, also exposed to the REST API.
 */
//noinspection TypeAnnotation
object WeatherForecast {
  implicit val dateTimeFormat = DateTimePattern.createLongFormatter
  implicit val storageFormat = Format[WeatherForecast](Json.reads[WeatherForecast], Json.writes[WeatherForecast])
}

case class WeatherForecast(
    location: String, // city[, country iso 2 letters]
    timestamp: DateTime,
    forecast: Weather
)
