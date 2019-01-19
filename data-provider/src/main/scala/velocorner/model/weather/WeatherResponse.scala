package velocorner.model.weather

import play.api.libs.json._

/**
  * The response structure from openweathermap.
  * https://openweathermap.org/forecast5#format
  */
object WeatherResponse {

  implicit val responseFormat = Format[WeatherResponse](Json.reads[WeatherResponse], Json.writes[WeatherResponse])
}

case class WeatherResponse(
  cod: String,
  list: List[Weather],
  city: City
)
