package velocorner.model

import play.api.libs.json._

/**
  * Created by levi on 27.11.16.
  */
object Statistics {
  val writes = new Writes[Statistics] {
    override def writes(o: Statistics): JsValue = {
      val baseJs: JsObject = Json.writes[Statistics].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Statistics")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val statisticsFormat = Format[Statistics](Json.reads[Statistics], writes)
}

case class Statistics(
  ytd_ride_totals: Total,
  all_ride_totals: Total
)