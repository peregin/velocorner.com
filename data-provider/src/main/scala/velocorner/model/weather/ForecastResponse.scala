package velocorner.model.weather

import play.api.libs.json._
import velocorner.api.weather.{City, Weather}

/**
 * The response structure from openweathermap.
 * https://openweathermap.org/forecast5#format
 */
object ForecastResponse {

  implicit val responseFormat = Format[ForecastResponse](Json.reads[ForecastResponse], Json.writes[ForecastResponse])
}

case class ForecastResponse(
    cod: String,
    list: Option[List[Weather]],
    city: Option[City]
) {
  lazy val points = list.getOrElse(List.empty)
}
