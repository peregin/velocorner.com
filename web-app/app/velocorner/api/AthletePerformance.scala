package velocorner.api

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

case class AthletePerformance(
    summary: Option[String],
    evaluating: Boolean,
    basedOn: String,
    createdAt: Option[DateTime]
)

object AthletePerformance {
  implicit val dateTimeFormat: Format[DateTime] = DateTimePattern.createLongFormatter
  implicit val format: Format[AthletePerformance] = Json.format[AthletePerformance]
}
