package velocorner.model.weather

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter


object SunriseSunsetInfo {
  implicit val dateTimeFormat = EpochFormatter.create
  implicit val infoFormat = Format[SunriseSunsetInfo](Json.reads[SunriseSunsetInfo], Json.writes[SunriseSunsetInfo])
}

case class SunriseSunsetInfo(
  sunrise: DateTime,
  sunset: DateTime
)

/**
  * The response structure from openweathermap.
  * https://openweathermap.org/current
  */
object WeatherResponse {
  implicit val responseFormat = Format[WeatherResponse](Json.reads[WeatherResponse], Json.writes[WeatherResponse])
}

case class WeatherResponse(
  sys: SunriseSunsetInfo
)
