package velocorner.api

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import org.specs2.mutable.Specification
import velocorner.model.DateTimePattern
import velocorner.util.JsonIo

class ActivitySpec extends Specification {

  "model" should {
    "use heart rate data" in {
      val hr = JsonIo.readReadFromResource[Activity]("/data/strava/activity-hr.json")
      hr.average_heartrate must beSome(159.6f)
      hr.max_heartrate must beSome(182.0f)
      hr.gear_id must beSome("b1494155")
    }

    "read ice skating activities" in {
      val ice = JsonIo.readReadFromResource[Activity]("/data/strava/ice_skating.json")
      ice.id === 2006731126
    }

    "read activity where upload_id is greater than int32" in {
      val activity = JsonIo.readReadFromResource[Activity]("/data/strava/fails.json")
      activity.id === 2010477317
      activity.upload_id === Some(2148810482L)
    }

    // 2014-03-30T02:51:36Z - bad - Illegal instant due to time zone offset transition, reading in Europe/Zurich - DST
    // 2015-01-23T16:18:17Z - good
    "read activity where start_date_local is missing and start_date is wrong" in {
      val startDate = DateTime.parse("2014-03-30T02:51:36Z",
        DateTimeFormat.forPattern(DateTimePattern.longFormat)
          .withZone(DateTimeZone.UTC) // otherwise takes current zone
      )
      startDate.getYear === 2014
      startDate.getHourOfDay === 2

      val activity = JsonIo.readReadFromResource[Activity]("/data/strava/no_start_date_local.json")
      activity.id === 126199417
      activity.upload_id === Some(138177658)
      activity.getStartDateLocal().getYear === 2014
      activity.getStartDateLocal().getHourOfDay === 13
    }
  }
}
