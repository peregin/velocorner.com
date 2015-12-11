package velocorner.model

import play.api.libs.json._

object Account {
  val writes = new Writes[Account] {
    override def writes(o: Account): JsValue = {
      val baseJs: JsObject = Json.writes[Account].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Account")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val accountFormat = Format[Account](Json.reads[Account], writes)

  def from(token: String, athlete: Athlete): Account = {
    new Account(athlete.id, athlete.firstname.orElse(athlete.lastname).getOrElse(""), athlete.profile_medium.getOrElse(""), token)
  }
}

case class Account(
  athleteId: Long,
  name: String,
  avatarUrl: String,
  accessToken: String
)
