package velocorner.api

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

object Achievement {
  implicit val dateTimeFormat = DateTimePattern.createLongFormatter
  implicit val achievementFormat = Format[Achievement](Json.reads[Achievement], Json.writes[Achievement])
}

case class Achievement(
    value: Double, // achievement value, example max speed, longest ride in km, etc.
    activityId: Long,
    activityName: String,
    activityTime: DateTime
) {
  def convert(fun: Double => Double): Achievement =
    this.copy(value = fun(this.value))
}
