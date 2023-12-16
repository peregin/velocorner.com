package velocorner.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.api.strava.Activity
import velocorner.model.strava.Athlete

import scala.io.Source

/**
 * Created by levi on 08/02/15.
 */
class JsonIoSpec extends AnyWordSpec with Matchers with CloseableResource {

  "converter for athlete activities" should {

    val json = withCloseable(Source.fromURL(getClass.getResource("/data/strava/last30activities.json")))(_.mkString)

    "read Strava activity" in {
      val list = JsonIo.read[List[Activity]](json)
      list must have size 30

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
      val jsonSingle = withCloseable(Source.fromURL(getClass.getResource("/data/strava/single_activity.json")))(_.mkString)
      val activity = JsonIo.read[Activity](jsonSingle)
      activity.max_speed mustBe Some(13.6f)
      activity.average_speed mustBe Some(4.732f)
      activity.average_cadence mustBe Some(64.9f)
    }
  }

  "converter for club activities" should {
    val json = withCloseable(Source.fromURL(getClass.getResource("/data/strava/club_activity.json")))(_.mkString)

    "read the json file" in {
      val list = JsonIo.read[List[Activity]](json)
      list must have size 1
    }
  }

  "writer for athlete" should {
    "generate type field" in {
      val a = Athlete(1, 2, Some("Levi"), None, None, Some("city"), Some("country"), None, None)
      val json = JsonIo.write(a)
      json must include("type\" : \"Athlete")
    }
  }

  "gzip file" should {
    "be parsed" in {
      val list = JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz")
      list must have size 200
    }
  }

}
