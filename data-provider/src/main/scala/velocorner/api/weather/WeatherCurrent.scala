package velocorner.api.weather

import play.api.libs.json.{Format, Json}
import velocorner.model.weather.SunriseSunsetInfo

/**
  * The structure from the storage layer.
  */
object WeatherCurrent {
  implicit val storageFormat = Format[WeatherCurrent](Json.reads[WeatherCurrent], Json.writes[WeatherCurrent])
}

case class WeatherCurrent(
    location: String, // city[, country iso 2 letters]
    timestamp: Long,
    current: WeatherDescription,
    info: WeatherInfo,
    sunrise: SunriseSunsetInfo
)
