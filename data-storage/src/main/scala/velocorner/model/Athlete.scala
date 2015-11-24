package velocorner.model

import play.api.libs.json.{Format, Json}
import play.api.libs.json._

/**
 * Created by levi on 22/03/15.
 */

object Athlete {
  val writes = new Writes[Athlete] {
    override def writes(o: Athlete): JsValue = {
      val baseJs: JsObject = Json.writes[Athlete].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Athlete")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val activityFormat = Format[Athlete](Json.reads[Athlete], writes)
}

case class Athlete(
  id:	Int,
  resource_state:	Int,
  firstname: Option[String],
  lastname: Option[String],
  profile_medium: Option[String] // URL to a 62x62 pixel profile picture
)
