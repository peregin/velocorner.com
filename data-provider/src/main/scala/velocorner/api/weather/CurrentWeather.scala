package velocorner.api.weather

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.EpochFormatter
import velocorner.model.weather.SunriseSunsetInfo

/**
  * The structure from the storage layer, also exposed to the REST API.
  */
object CurrentWeather {
  implicit val dateTimeFormat = EpochFormatter.create
  implicit val storageFormat = Format[CurrentWeather](Json.reads[CurrentWeather], Json.writes[CurrentWeather])
}

case class CurrentWeather(
    location: String, // city[, country iso 2 letters]
    timestamp: DateTime,
    current: WeatherDescription,
    info: WeatherInfo,
    sunriseSunset: SunriseSunsetInfo
)
