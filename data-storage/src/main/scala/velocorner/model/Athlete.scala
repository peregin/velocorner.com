package velocorner.model

import play.api.libs.json.{Format, Json}
import play.api.libs.json._

/**
  * Represents an athlete from the feed and storage layer.
  *
  * The feed provides the following superset of data:
  * "id": 227615,
  * "resource_state": 2,
  * "firstname": "John",
  * "lastname": "Applestrava",
  * "profile_medium": "http://pics.com/227615/medium.jpg",
  * "profile": "http://pics.com/227615/large.jpg",
  * "city": "San Francisco",
  * "state": "CA",
  * "country": "United States",
  * "sex": "M",
  * "friend": null,
  * "follower": "accepted",
  * "premium": true,
  * "created_at": "2011-03-19T21:59:57Z",
  * "updated_at": "2013-09-05T16:46:54Z"
  *
  */
object Athlete {
  val writes = new Writes[Athlete] {
    override def writes(o: Athlete): JsValue = {
      val baseJs: JsObject = Json.writes[Athlete].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Athlete")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val activityFormat = Format[Athlete](Json.reads[Athlete], writes)
}

case class Athlete(
  id:	Int,
  resource_state:	Int,
  firstname: Option[String],
  lastname: Option[String],
  profile_medium: Option[String], // URL to a 62x62 pixel profile picture
  city: Option[String],
  country: Option[String]
)
