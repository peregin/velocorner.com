package velocorner.model.weather

import play.api.libs.json.{Format, Json}

case class WeatherCode(code: Int, meaning: String, bootstrapIcon: String)

object WeatherCode {

  implicit val codeFormat = Format[WeatherCode](Json.reads[WeatherCode], Json.writes[WeatherCode])
}
