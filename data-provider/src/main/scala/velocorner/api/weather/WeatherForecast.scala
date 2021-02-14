package velocorner.api.weather

import play.api.libs.json.{Format, Json}

/** The structure from the storage layer.
  */
object WeatherForecast {
  implicit val storageFormat = Format[WeatherForecast](Json.reads[WeatherForecast], Json.writes[WeatherForecast])
}

case class WeatherForecast(
    location: String, // city[, country iso 2 letters]
    timestamp: Long,
    forecast: Weather
)
