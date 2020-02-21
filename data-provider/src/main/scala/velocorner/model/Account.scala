package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.api.Athlete

object Account {
  implicit val dateTimeFormat = DateTimePattern.createLongFormatter

  val writes = new Writes[Account] {
    override def writes(o: Account): JsValue = {
      val baseJs: JsObject = Json.writes[Account].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Account")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val accountFormat = Format[Account](Json.reads[Account], writes)

  def from(athlete: Athlete, token: String, lastUpdate: Option[DateTime]) = new Account(
    athlete.id,
    athlete.firstname.orElse(athlete.lastname).getOrElse(""),
    s"${athlete.city.mkString}, ${athlete.country.mkString}",
    athlete.profile_medium.getOrElse(""),
    accessToken = token,
    lastUpdate
  )
}

/**
 * Represents a generic account used in the storage layer.
 */
case class Account(
  athleteId: Long,
  displayName: String,
  displayLocation: String, // city, country
  avatarUrl: String,
  accessToken: String,
  lastUpdate: Option[DateTime]
)
