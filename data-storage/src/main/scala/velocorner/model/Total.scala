package velocorner.model

import play.api.libs.json.{Format, Json}

/**
  * Created by levi on 27.11.16.
  */
object Total {
  implicit val totalFormat = Format[Total](Json.reads[Total], Json.writes[Total])
}

case class Total(
  count: Float,
  distance: Float
)
