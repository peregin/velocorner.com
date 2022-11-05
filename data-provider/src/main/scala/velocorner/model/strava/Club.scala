package velocorner.model.strava

import play.api.libs.json._

/**
 * Represents a Club identifier from the Strava feed.
 *
 * Club activity list:
 *
 * {"id":266650480,"resource_state":2,"external_id":"garmin_push_717236357","upload_id":301350809,
 * "athlete":{"id":432909,"resource_state":2,"firstname":"Levente","lastname":"Fall","profile_medium":"https://dgalywyr863hv.cloudfront.net/pictures/athletes/432909/866647/2/medium.jpg","profile":"https://dgalywyr863hv.cloudfront.net/pictures/athletes/432909/866647/2/large.jpg","city":"Adliswil","state":"Z\u00fcrich","country":"Switzerland","sex":"M","friend":null,"follower":null,"premium":false,"created_at":"2012-04-30T16:43:08Z","updated_at":"2015-03-08T09:46:57Z","badge_type_id":0},
 * "name":"Morning Ride",
 * "distance":6646.6,"moving_time":973,"elapsed_time":1039,
 * "total_elevation_gain":22.0,"type":"Ride",
 * "start_date":"2015-03-11T07:21:22Z",
 * "start_date_local":"2015-03-11T08:21:22Z",
 * "timezone":"(GMT+01:00) Europe/Zurich",
 * "start_latlng":[47.31,8.52],"end_latlng":[47.37,8.53],
 * "location_city":"Adliswil","location_state":"Canton of Zurich","location_country":"Switzerland",
 * "start_latitude":47.31,"start_longitude":8.52,"achievement_count":0,"kudos_count":0,"comment_count":0,
 * "athlete_count":1,"photo_count":0,
 * "map":{"id":"a266650480","summary_polyline":"_ux_H{p~r@o_@jQiGuGeCMmIfDoErKaFd@_a@kf@uFa@sExEeFsEiQp@sb@gDiFwDaX{^}CBiE`EqUTwTzEiF{IwL}CqC@sE~D",
 * "resource_state":2},
 * "trainer":false,"commute":false,"manual":false,"private":false,"flagged":false,"gear_id":"b1494155",
 * "average_speed":6.831,"max_speed":13.0,"average_cadence":77.7,"average_temp":8.0,"average_watts":182.2,"kilojoules":177.3,"device_watts":false,
 * "truncated":23,"has_kudoed":false}]
 */
object Club {

  val Velocorner = 122614

  val writes = new Writes[Club] {
    override def writes(o: Club): JsValue = {
      val baseJs: JsObject = Json.writes[Club].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Club")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }
  implicit val clubFormat = Format[Club](Json.reads[Club], writes)
}

case class Club(id: Int, memberIds: List[Int])
