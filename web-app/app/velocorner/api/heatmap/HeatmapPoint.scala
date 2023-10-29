package velocorner.api.heatmap

import play.api.libs.json.{Format, Json}

object HeatmapPoint {
  implicit val pointFormat: Format[HeatmapPoint] = Format[HeatmapPoint](Json.reads[HeatmapPoint], Json.writes[HeatmapPoint])
}

case class HeatmapPoint(x: String, y: Long)
