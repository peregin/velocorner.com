package velocorner.api.strava

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.model.DateTimePattern
import velocorner.util.JsonIo

class ActivitySpec extends AnyWordSpec with Matchers {

  "model" should {
    "use heart rate data" in {
      val hr = JsonIo.readReadFromResource[Activity]("/data/strava/activity-hr.json")
      hr.average_heartrate mustBe Some(159.6f)
      hr.max_heartrate mustBe Some(182.0f)
      hr.gear_id mustBe Some("b1494155")
    }

    "read ice skating activities" in {
      val ice = JsonIo.readReadFromResource[Activity]("/data/strava/ice_skating.json")
      ice.id === 2006731126L
    }

    "read activity where upload_id is greater than int32" in {
      val activity = JsonIo.readReadFromResource[Activity]("/data/strava/fails.json")
      activity.id === 2010477317L
      activity.upload_id === Some(2148810482L)
    }

    // 2014-03-30T02:51:36Z - bad - Illegal instant due to time zone offset transition, reading in Europe/Zurich - DST
    // 2015-01-23T16:18:17Z - good
    "read activity where start_date_local is missing and start_date is wrong" in {
      val startDate = DateTime.parse(
        "2014-03-30T02:51:36Z",
        DateTimeFormat
          .forPattern(DateTimePattern.longFormat)
          .withZone(DateTimeZone.UTC) // otherwise takes current zone
      )
      startDate.getYear === 2014
      startDate.getHourOfDay === 2

      val activity = JsonIo.readReadFromResource[Activity]("/data/strava/no_start_date_local.json")
      activity.id === 126199417L
      activity.upload_id === Some(138177658L)
      activity.getStartDateLocal().getYear === 2014
      activity.getStartDateLocal().getHourOfDay === 13
    }
  }
}
