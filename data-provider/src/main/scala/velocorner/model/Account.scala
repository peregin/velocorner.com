package velocorner.model

import cats.implicits._
import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.strava.Athlete

import scala.util.{Failure, Success, Try}

object Account {

  def convert(s: String): Units.Entry = s.toLowerCase match {
    case "metric" => Units.Metric
    case "imperial" => Units.Imperial
    case other => throw new IllegalArgumentException(s"not a valid unit $other")
  }

  implicit val roleFormat = Format[Role.Entry]((json: JsValue) => json match {
    case JsString(s) => s match {
      case "admin" => JsSuccess(Role.Admin)
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.role.format", "admin"))))
    }
    case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.role"))))
  }, (o: Role.Entry) => JsString(o.toString.toLowerCase))

  implicit val unitFormat = Format[Units.Entry]((json: JsValue) => json match {
    case JsString(s) => Try(convert(s)) match {
      case Success(unit) => JsSuccess(unit)
      case Failure(ex) => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.unit.format", ex.getMessage))))
    }
    case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.unit"))))
  }, (o: Units.Entry) => JsString(o.toString.toLowerCase))

  implicit val dateTimeFormat = DateTimePattern.createLongFormatter

  val writes = new Writes[Account] {
    override def writes(o: Account): JsValue = {
      val baseJs: JsObject = Json.writes[Account].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Account")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val accountFormat = Format[Account](Json.reads[Account], writes)

  // extract the user details from provider, e.g. Strava into the consumer one (velocorner.com)
  def from(
      athlete: Athlete,
      stravaAccess: OAuth2Access,
      lastUpdate: Option[DateTime],
      role: Option[Role.Entry],
      unit: Option[Units.Entry]
  ) = new Account(
    athlete.id,
    athlete.firstname.orElse(athlete.lastname).getOrElse(""),
    displayLocation = s"${athlete.city.mkString}, ${athlete.country.mkString}",
    athlete.profile_medium.getOrElse(""),
    lastUpdate = lastUpdate,
    role = role,
    unit = unit,
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
    role: Option[Role.Entry], // admin or regular user
    unit: Option[Units.Entry], // user preference for units of measurement (metric vs imperial)
    stravaAccess: Option[OAuth2Access]
) {

  def isAdmin(): Boolean = role.exists(_ == Role.Admin)

  def units(): Units.Entry = unit.getOrElse(Units.Metric)
  def isImperial(): Boolean = unit.exists(_ == Units.Imperial)
}
