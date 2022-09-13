package velocorner.api.chart

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

object DailyPoint {
  implicit val dateFormat = DateTimePattern.createShortFormatter
  implicit val pointFormat = Format[DailyPoint](Json.reads[DailyPoint], Json.writes[DailyPoint])
}

// getMonthOfYear - in javascript date starts with 0
case class DailyPoint(day: LocalDate, value: Double)
