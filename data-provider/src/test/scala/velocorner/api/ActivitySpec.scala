package velocorner.api

import org.specs2.mutable.Specification
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
  }
}
