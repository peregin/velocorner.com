package velocorner.model.weather

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter

/**
  * https://openweathermap.org/forecast5#format
  */
object WeatherDescription {
  implicit val responseFormat = Format[WeatherDescription](Json.reads[WeatherDescription], Json.writes[WeatherDescription])
}

case class WeatherDescription(
  id: Long, // 800 - weather condition code
  main: String, // "Clear"
  description: String // "clear sky"
)

object WeatherInfo {
  implicit val responseFormat = Format[WeatherInfo](Json.reads[WeatherInfo], Json.writes[WeatherInfo])
}

case class WeatherInfo(
  temp: Float,
  temp_min: Float,
  temp_max: Float,
  humidity: Float
)

object Weather {

  implicit val dateTimeFormat = EpochFormatter.create
  val writes = new Writes[Weather] {
    override def writes(o: Weather): JsValue = {
      val baseJs: JsObject = Json.writes[Weather].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Weather")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val weatherFormat = Format[Weather](Json.reads[Weather], writes)
}

case class Weather(
  dt: DateTime,
  main: WeatherInfo,
  weather: List[WeatherDescription]
)

object City {
  implicit val responseFormat = Format[City](Json.reads[City], Json.writes[City])
}

case class City(
  id: Long,
  name: String, // plain name, such as Zurich, Budapest
  country: String // ISO code 2 letter
)
