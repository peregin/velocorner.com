package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class ActivitySpec extends Specification {

  "model" should {
    "use heart rate data" in {
      val hr = JsonIo.readReadFromResource[Activity]("/data/strava/activity-hr.json")
      hr.average_heartrate must beSome(159.6f)
      hr.max_heartrate must beSome(182.0f)
    }
  }
}
