package velocorner.api.heatmap

import play.api.libs.json.{Format, Json}

object HeatmapSeries {
  implicit val seriesFormat: Format[HeatmapSeries] = Format[HeatmapSeries](Json.reads[HeatmapSeries], Json.writes[HeatmapSeries])
}

case class HeatmapSeries(name: String, data: List[HeatmapPoint])
