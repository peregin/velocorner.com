package velocorner.api.weather

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter

/**
  * Structure from the storage layer and exposed in the external API.
  */
object SunriseSunset {
  implicit val dateTimeFormat = EpochFormatter.create

  val writes = new Writes[SunriseSunset] {
    override def writes(o: SunriseSunset): JsValue = {
      val baseJs: JsObject = Json.writes[SunriseSunset].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Sun")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val storageFormat = Format[SunriseSunset](Json.reads[SunriseSunset], writes)
}

case class SunriseSunset
(
  location: String, // city[, country iso 2 letters]
  date: String, // simple iso format 2019-02-12, ISO8601
  sunrise: DateTime,
  sunset: DateTime
)
