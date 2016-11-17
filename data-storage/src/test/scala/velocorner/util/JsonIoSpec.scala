package velocorner.util

import org.specs2.mutable.Specification
import velocorner.model.{Athlete, Activity}

import scala.io.Source

/**
 * Created by levi on 08/02/15.
 */
class JsonIoSpec extends Specification {

  stopOnFail

  "converter for athlete activities" should {

    val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString

    "read Strava activity" in {
      val list = JsonIo.read[List[Activity]](json)
      list must haveSize(30)

      val first = list.head
      first.name === "Stallikon Ride"
      first.distance === 23216.8f
    }

    "read and write" in {
      val list = JsonIo.read[List[Activity]](json)
      val activity = list.head

      val jsonText = JsonIo.write(activity)
      val otherActivity = JsonIo.read[Activity](jsonText)
      activity === otherActivity
    }

    "read all the activity details" in {
      val jsonSingle = Source.fromURL(getClass.getResource("/data/strava/single_activity.json")).mkString
      val activity = JsonIo.read[Activity](jsonSingle)
      activity.max_speed must beSome(13.6f)
      activity.average_speed must beSome(4.732f)
      activity.average_cadence must beSome(64.9f)
    }
  }

  "converter for club activities" should {
    val json = Source.fromURL(getClass.getResource("/data/strava/club_activity.json")).mkString

    "read the json file" in {
      val list = JsonIo.read[List[Activity]](json)
      list must haveSize(1)
    }
  }

  "writer for athlete" should {
    "generate type field" in {
      val a = Athlete(1, 2, Some("Levi"), None, None, Some("city"), Some("country"))
      val json = JsonIo.write(a)
      json must contain("type\" : \"Athlete")
    }
  }

}
