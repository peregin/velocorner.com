package velocorner.model

import play.api.libs.json.{Json, Format}

/**
 * Created by levi on 22/03/15.
 */

object Athlete {
  implicit val activityFormat = Format[Athlete](Json.reads[Athlete], Json.writes[Athlete])
}

case class Athlete(
  id:	Int,
  resource_state:	Int,
  firstname: Option[String],
  lastname: Option[String])
