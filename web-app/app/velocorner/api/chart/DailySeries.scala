package velocorner.api.chart

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}

object DailySeries {
  implicit val seriesFormat: Format[DailySeries] = Format[DailySeries](Json.reads[DailySeries], Json.writes[DailySeries])
}

case class DailySeries(name: String, series: List[DailyPoint]) {

  def aggregate(): DailySeries = {
    val aggregatedSeries =
      series.reverse.scanLeft(DailyPoint(LocalDate.now(), 0))((accu, i) => DailyPoint(i.day, accu.value + i.value)).tail
    DailySeries(name, aggregatedSeries)
  }
}
