package velocorner.api.weather

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter

/**
  * https://openweathermap.org/forecast5#format
  */
object WindDescription {
  implicit val responseFormat = Format[WindDescription](Json.reads[WindDescription], Json.writes[WindDescription])
}

case class WindDescription(
                            speed: Double, // m/s
                            deg: Double    // degrees
                          )

object CloudDescription {
  implicit val responseFormat = Format[CloudDescription](Json.reads[CloudDescription], Json.writes[CloudDescription])
}

case class CloudDescription(
                             all: Int // %
                           )

object RainDescription {
  implicit val responseFormat = Format[RainDescription](Json.reads[RainDescription], Json.writes[RainDescription])
}

case class RainDescription(
                            `3h`: Option[Double] // mm
                          )

object SnowDescription {
  implicit val responseFormat = Format[SnowDescription](Json.reads[SnowDescription], Json.writes[SnowDescription])
}

case class SnowDescription(
                            `3h`: Option[Double] // volume
                          )

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

// one of the entry points, the response contains a list of Weather structures, also stored in database
case class Weather(
                    dt: DateTime,
                    main: WeatherInfo,
                    weather: List[WeatherDescription],
                    snow: Option[SnowDescription],
                    rain: Option[RainDescription],
                    clouds: CloudDescription,
                    wind: WindDescription
                  )

// the list of Weather structures is associated with a city as well
object City {
  implicit val responseFormat = Format[City](Json.reads[City], Json.writes[City])
}

case class City(
                 id: Long,
                 name: String, // plain name, such as Zurich, Budapest
                 country: String // ISO code 2 letter
               )

