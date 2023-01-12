package velocorner.api.strava

import ai.x.play.json.{CamelToSnakeNameEncoder, Jsonx, NameEncoder}
import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.DateTimePattern
import velocorner.model.strava.Athlete

/**
 * Represents an Activity from the Strava feed and storage layer:
 * <code>
 *
 * [
 *  {
 *    "id": 805296924,
 *    "resource_state": 2,
 *    "external_id": "garmin_push_1487499723",
 *    "upload_id": 892075432,
 *    "athlete": {
 *      "id": 432909,
 *      "resource_state": 1
 *    },
 *    "name": "Sihltal Round",
 *    "distance": 29154.4, -- in meters
 *    "moving_time": 6450, -- in seconds
 *    "elapsed_time": 6768,
 *    "total_elevation_gain": 638.0, -- in meters
 *    "type": "Ride",
 *    "start_date": "2016-12-18T09:56:53Z",
 *    "start_date_local": "2016-12-18T10:56:53Z",
 *    "timezone": "(GMT+01:00) Europe/Zurich",
 *    "start_latlng": [
 *      47.31,
 *      8.52
 *    ],
 *    "end_latlng": [
 *      47.31,
 *      8.52
 *    ],
 *    "location_city": "Adliswil",
 *    "location_state": "Canton of Zurich",
 *    "location_country": "Switzerland",
 *    "start_latitude": 47.31,
 *    "start_longitude": 8.52,
 *    "achievement_count": 3,
 *    "kudos_count": 10,
 *    "comment_count": 0,
 *    "athlete_count": 1,
 *    "photo_count": 0,
 *    "map": {
 *      "id": "a805296924",
 *      "summary_polyline": "w~v_Hwf_s@|\\UrH`C|EgDt@uCzNaG|ZyFbHiMtOsKb^nDtRkRb[mI|TeUxDbB|AgJ`I}@zI_X|J_FxMkBpFoD~AmEpDNxGiG|MiAhZyJt@kF`FyClFkJ`J{CjGuQxF{GxKrBj@sFjCmEzBKjSuRrGgBpFyFvLkCbEpBtJ{GrD~AxLkBtGiB`HoJjAjExAwAk@cGzB{FrGiDaAHe@yObElCxF_OrLfIh@eJoBsGPwClGpH`EiBu@aOkCgCoAqFwDn@gDoGAoC|BwCPmQjAcFu@oF{@iB_IsAyBgGaFbCoDiBiEaOm@mIeFzC_DtImGrDqJ~Lu\\bHyRzIsEBsYsJwNLaDfFsIfD}P~AqShPmH`Ny]bc@gEzByOvj@eIbMyRm@_S|IiQdS}LrGgBtHqFnGyCfNoAMiCoJeHdAyHrQwBdRuTjNuUqAyF`CiGjJmHjF{EdLuJj@eH_EuUbLcJAY`G{K|d@iGpOnAh[",
 *      "resource_state": 2
 *    },
 *    "trainer": false,
 *    "commute": false,
 *    "manual": false,
 *    "private": false,
 *    "flagged": false,
 *    "gear_id": "b1494155",
 *    "average_speed": 4.52,
 *    "max_speed": 15.0,
 *    "average_cadence": 76.9,
 *    "average_temp": 0.0,
 *    "average_watts": 152.4,
 *    "kilojoules": 982.9,
 *    "device_watts": false,
 *    "has_heartrate": false,
 *    "elev_high": 787.0,
 *    "elev_low": 458.0,
 *    "pr_count": 1,
 *    "total_photo_count": 6,
 *    "has_kudoed": false,
 *    "workout_type": 10,
 *    "suffer_score": null
 *  }
 * ]
 *
 * </code>
 */

object Activity {

  implicit val dateTimeFormat = DateTimePattern.createLongFormatter
  // generates a PlayJson Format[T] for a case class T with any number of fields
  implicit val encoder: NameEncoder = CamelToSnakeNameEncoder()
  implicit val activityFormat: OFormat[Activity] = Jsonx.formatCaseClass[Activity]
  implicit val listActivities: Reads[List[Activity]] = Reads.list(activityFormat)
}

// max 22 fields are supported by the regular json marshaller, using ai.x.play extension
case class Activity(
    id: Long,
    resource_state: Int,
    external_id: Option[String],
    upload_id: Option[Long],
    athlete: Athlete,
    name: String,
    distance: Float,
    moving_time: Int, // in seconds
    elapsed_time: Int,
    total_elevation_gain: Float,
    `type`: String,
    start_date: DateTime,
    start_date_local: Option[DateTime],
    average_speed: Option[Float],
    max_speed: Option[Float],
    average_cadence: Option[Float],
    average_temp: Option[Float],
    average_watts: Option[Float],
    max_watts: Option[Float],
    average_heartrate: Option[Float],
    max_heartrate: Option[Float],
    gear_id: Option[String],
    start_latitude: Option[Float],
    start_longitude: Option[Float],
    commute: Option[Boolean],
    elev_high: Option[Float],
    elev_low: Option[Float],
    pr_count: Option[Int]
) {
  // for some devices the local_start_date is not set
  def getStartDateLocal(): DateTime = start_date_local.getOrElse(start_date)
}
