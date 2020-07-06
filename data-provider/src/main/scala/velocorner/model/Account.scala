package velocorner.model

import cats.implicits._
import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.strava.Athlete

object StravaAccess {
  implicit val dateTimeFormat = DateTimePattern.createLongFormatter
  implicit val dataFormat = Format[StravaAccess](Json.reads[StravaAccess], Json.writes[StravaAccess])
}

case class StravaAccess(
                         accessToken: String,
                         accessExpiresAt: DateTime,
                         refreshToken: String
                       )

object Account {

  implicit val roleFormat = Format[Role.Entry]((json: JsValue) => json match {
    case JsString(s) => s match {
      case "admin" => JsSuccess(Role.Admin)
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.role.format", "admin"))))
    }
    case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.role"))))
  }, (o: Role.Entry) => JsString(o.toString.toLowerCase))

  implicit val dateTimeFormat = DateTimePattern.createLongFormatter

  val writes = new Writes[Account] {
    override def writes(o: Account): JsValue = {
      val baseJs: JsObject = Json.writes[Account].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Account")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val accountFormat = Format[Account](Json.reads[Account], writes)

  // extract the user details from provider, e.g. Stava into the consumer one (velocorner.com)
  def from(athlete: Athlete, stravaAccessToken: String, stravaAccessExpiresAt: DateTime, stravaRefreshToken: String) = new Account(
    athlete.id,
    athlete.firstname.orElse(athlete.lastname).getOrElse(""),
    s"${athlete.city.mkString}, ${athlete.country.mkString}",
    athlete.profile_medium.getOrElse(""),
    lastUpdate = none,
    role = none,
    stravaAccess = StravaAccess(
      accessToken = stravaAccessToken,
      accessExpiresAt = stravaAccessExpiresAt,
      refreshToken = stravaRefreshToken
    ).some
  )
}

/**
  * Represents a generic account used in the storage layer.
  */
case class Account(
                    athleteId: Long,
                    displayName: String, // first name
                    displayLocation: String, // city, country
                    avatarUrl: String,
                    lastUpdate: Option[DateTime],
                    role: Option[Role.Entry],
                    stravaAccess: Option[StravaAccess]
                  ) {

  def isAdmin(): Boolean = role.exists(_ == Role.Admin)
}
