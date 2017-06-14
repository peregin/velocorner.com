package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

object Account {
  val writes = new Writes[Account] {
    override def writes(o: Account): JsValue = {
      val baseJs: JsObject = Json.writes[Account].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Account")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val dateTimeFormat = Format[DateTime](Reads.jodaDateReads(DateTimePattern.format), Writes.jodaDateWrites(DateTimePattern.format))
  implicit val accountFormat = Format[Account](Json.reads[Account], writes)

  def from(athlete: Athlete, token: String, lastUpdate: Option[DateTime]) = new Account(
    athlete.id,
    athlete.firstname.orElse(athlete.lastname).getOrElse(""),
    s"${athlete.city.mkString}, ${athlete.country.mkString}",
    athlete.profile_medium.getOrElse(""),
    token,
    lastUpdate
  )
}

case class Account(
  athleteId: Int,
  displayName: String,
  displayLocation: String,
  avatarUrl: String,
  accessToken: String,
  lastUpdate: Option[DateTime]
)
