package velocorner.model

import cats.implicits._
import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.strava.Athlete


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
  def from(athlete: Athlete, stravaAccess: OAuth2Access, lastUpdate: Option[DateTime], role: Option[Role.Entry]) = new Account(
    athlete.id,
    athlete.firstname.orElse(athlete.lastname).getOrElse(""),
    s"${athlete.city.mkString}, ${athlete.country.mkString}",
    athlete.profile_medium.getOrElse(""),
    lastUpdate = lastUpdate,
    role = role,
    stravaAccess = stravaAccess.some
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
                    stravaAccess: Option[OAuth2Access]
                  ) {

  def isAdmin(): Boolean = role.exists(_ == Role.Admin)
}
