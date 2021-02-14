package velocorner.model.strava

import play.api.libs.json._

object Gear {

  sealed abstract class Entry
  case object Bike extends Entry
  case object Shoe extends Entry

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
