package velocorner.model.weather

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter

/**
  * https://openweathermap.org/forecast5#format
  */
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
                    dt: DateTime
                  )

object WeatherResponse {

  implicit val responseFormat = Format[WeatherResponse](Json.reads[WeatherResponse], Json.writes[WeatherResponse])
}

case class WeatherResponse(
                            cod: String,
                            list: List[Weather]
                          )
