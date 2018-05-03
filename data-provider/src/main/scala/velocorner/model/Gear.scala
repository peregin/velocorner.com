package velocorner.model

import play.api.libs.json._

object Gear {

  val writes = new Writes[Gear] {
    override def writes(o: Gear): JsValue = {
      val baseJs: JsObject = Json.writes[Gear].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Gear")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val gearFormat = Format[Gear](Json.reads[Gear], writes)
}

case class Gear(id: String, name: String, distance: Float)