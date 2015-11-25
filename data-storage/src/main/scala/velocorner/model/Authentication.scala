package velocorner.model

import play.api.libs.json.{Json, Format}

/**
  * Created by levi on 09/11/15.
  */
object Authentication {
  implicit val authenticationFormat = Format[Authentication](Json.reads[Authentication], Json.writes[Authentication])
}

case class Authentication(access_token: String, athlete: Athlete)
