package velocorner.api.chart

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}

object DailySeries {
  implicit val seriesFormat = Format[DailySeries](Json.reads[DailySeries], Json.writes[DailySeries])
}

case class DailySeries(name: String, series: Iterable[DailyPoint]) {

  def aggregate(): DailySeries = {
    val aggregatedSeries = series.toSeq.reverse.scanLeft(DailyPoint(LocalDate.now(), 0))((accu, i) =>
      DailyPoint(i.day, accu.value + i.value)).tail
    DailySeries(name, aggregatedSeries)
  }
}
