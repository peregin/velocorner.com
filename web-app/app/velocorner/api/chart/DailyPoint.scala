package velocorner.api.chart

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

object DailyPoint {
  implicit val dateFormat = DateTimePattern.createShortFormatter
  implicit val pointFormat = Format[DailyPoint](Json.reads[DailyPoint], Json.writes[DailyPoint])
}

case class DailyPoint(day: LocalDate, value: Double) {

  def getMonth: Int = day.getMonthOfYear - 1 // in javascript date starts with 0
  def getDay: Int = day.getDayOfMonth
}
