package velocorner.api.weather

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.EpochFormatter
import velocorner.model.weather.SunriseSunsetInfo

object WeatherDescription {
  implicit val responseFormat = Format[WeatherDescription](Json.reads[WeatherDescription], Json.writes[WeatherDescription])
}

case class WeatherDescription(
    id: Long, // 800 - weather condition code
    main: String, // "Clear"
    description: String, // "clear sky"
    icon: String // weather icon 04d
)

object WeatherInfo {
  implicit val responseFormat = Format[WeatherInfo](Json.reads[WeatherInfo], Json.writes[WeatherInfo])
}

case class WeatherInfo(
    temp: Float, // C
    temp_min: Float, // C
    temp_max: Float, // C
    humidity: Float, // %
    pressure: Float // hPa
)

object Coord {
  implicit val coordFormat = Format[Coord](Json.reads[Coord], Json.writes[Coord])
}

case class Coord(lon: Double, lat: Double)

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
    bootstrapIcon: String, // bootstrap icon derived from the current weather code
    current: WeatherDescription,
    info: WeatherInfo,
    sunriseSunset: SunriseSunsetInfo,
    coord: Coord
)
