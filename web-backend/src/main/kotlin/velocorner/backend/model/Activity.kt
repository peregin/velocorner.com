package velocorner.backend.model

import java.time.OffsetDateTime

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
data class Activity(
    val id: Long,
    val resource_state: Int,
    val external_id: String?,
    val uploadId: Long?,
    val athlete: Athlete,
    val name: String,
    val distance: Double,
    val moving_time: Int, // in seconds
    val elapsed_time: Int,
    val total_elevation_gain: Double,
    val type: String,
    val start_date: OffsetDateTime,
    val start_date_local: OffsetDateTime?,
    val average_speed: Float?,
    val max_speed: Float?,
    val average_cadence: Float?,
    val average_temp: Float?
) {
    fun getStartDateLocal(): OffsetDateTime = start_date_local ?: start_date
}
