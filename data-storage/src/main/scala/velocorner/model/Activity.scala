package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json.{Writes, Reads, Json, Format}

/**
 *
 * Activity list:

 [{"id":244993130,"resource_state":2,"external_id":"garmin_push_680641525","upload_id":277374426,
 "athlete":{"id":432909,"resource_state":1},
 "name":"Stallikon Ride",
 "distance":23216.8,"moving_time":4906,"elapsed_time":4906,
 "total_elevation_gain":541.0,"type":"Ride",
 "start_date":"2015-01-23T16:18:17Z",
 "start_date_local":"2015-01-23T17:18:17Z",
 "timezone":"(GMT+01:00) Europe/Zurich",
 "start_latlng":[47.37,8.52],"end_latlng":[47.31,8.52],
 "location_city":"Zurich","location_state":"Canton of Zurich","location_country":"Switzerland",
 "start_latitude":47.37,"start_longitude":8.52,"achievement_count":2,"kudos_count":4,"comment_count":0,
 "athlete_count":1,"photo_count":0,
 "map":{"id":"a244993130","summary_polyline":"qfb`Ha_`s@sMfRXpJ}@dB`BnM`KpQkJrN|Gza@jHfSbB?lLpXHnIzBbI}DtFgHhVqMtSoA|Of@nEeNh]KrLoCpRt@z]nEpUzFfKrJjEtKkMfNnB~GyHnHl@xImKrKy@pC_NwByGhAaE~HhApI}OvKmM~OsKlFkKlKyKpJyChc@q]vr@wJdk@uBjKtDdHoHnZmMpEbB`W``@zMpHdL~@`ViI`@ePcBgEqQzGzI{OkB{JO{O_DsH@{C|CeGzGyBzH{JvIoXlEiB|BqPhFl@jDoGU{AmHe@tCwLrFgFqHEcOpH_FmClB}@x@{JnEaF@yHbCXbBwF~GaHWwLzAiJg\\nIkKXsJnNs@|FwD~B}IgCcN`BeOcA",
 "resource_state":2},
 "trainer":false,"commute":false,"manual":false,"private":false,"flagged":false,"gear_id":"b1494155",
 "average_speed":4.732,"max_speed":13.6,"average_cadence":64.9,"average_temp":-1.0,"average_watts":172.1,"kilojoules":844.1,"device_watts":false,
 "truncated":1622,"has_kudoed":false}]

 *
 */

object Activity {

  val dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  implicit val dateTimeFormat = Format[DateTime](Reads.jodaDateReads(dateTimePattern), Writes.jodaDateWrites(dateTimePattern))
  implicit val activityFormat = Format[Activity](Json.reads[Activity], Json.writes[Activity])
}

case class Activity(
  id: Int,
  resource_state: Int,
  external_id: Option[String],
  upload_id: Option[Int],
  athlete: Map[String, Int],
  name: String,
  distance: Float,
  moving_time: Int,
  elapsed_time: Int,
  total_elevation_gain: Float,
  `type`: String,
  start_date: DateTime,
  start_date_local: DateTime,
  timezone: String
)
