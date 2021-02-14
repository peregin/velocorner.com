package velocorner.model.strava

import play.api.libs.json.{Format, Json, _}

/** Represents an athlete from the Strava feed and storage layer.
  *
  * The feed provides the following superset of data:
  *
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
  * "bikes" : [ {
  *   "id" : "b12345678987655",
  *   "primary" : true,
  *   "name" : "EMC",
  *   "resource_state" : 2,
  *   "distance" : 0
  * } ],
  * "shoes" : [ {
  *   "id" : "g12345678987655",
  *   "primary" : true,
  *   "name" : "adidas",
  *   "resource_state" : 2,
  *   "distance" : 4904
  * } ]
  */
object Athlete {
  val writes: Writes[Athlete] = (o: Athlete) => {
    val baseJs: JsObject = Json.writes[Athlete].writes(o).as[JsObject]
    val typeJs: JsString = Writes.StringWrites.writes("Athlete")
    JsObject(baseJs.fields :+ ("type" -> typeJs))
  }
  implicit val athleteFormat: Format[Athlete] = Format[Athlete](Json.reads[Athlete], writes)
  implicit val listAthletes: Reads[List[Athlete]] = Reads.list(athleteFormat)
}

case class Athlete(
    id: Long,
    resource_state: Int,
    firstname: Option[String],
    lastname: Option[String],
    profile_medium: Option[String], // URL to a 62x62 pixel profile picture
    city: Option[String],
    country: Option[String],
    bikes: Option[List[Gear]],
    shoes: Option[List[Gear]]
)
