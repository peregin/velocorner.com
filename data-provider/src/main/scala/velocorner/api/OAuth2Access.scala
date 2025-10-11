package velocorner.api

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern

object OAuth2Access {
  implicit val dateTimeFormat: Format[DateTime] = DateTimePattern.createLongFormatter
  implicit val dataFormat: Format[OAuth2Access] = Format[OAuth2Access](Json.reads[OAuth2Access], Json.writes[OAuth2Access])
}

case class OAuth2Access(
    accessToken: String,
    accessExpiresAt: DateTime,
    refreshToken: String
)
