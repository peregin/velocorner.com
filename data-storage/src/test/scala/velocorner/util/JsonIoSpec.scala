package velocorner.util

import org.specs2.mutable.Specification
import velocorner.model.Activity

import scala.io.Source

/**
 * Created by levi on 08/02/15.
 */
class JsonIoSpec extends Specification {

  addArguments(stopOnFail)

  "converter" should {

    "read Strava activity" in {
      val json = Source.fromURL(getClass.getResource("/data/strava/last10activities.txt")).mkString
      val list = JsonIo.readFromText[List[Activity]](json)
      list must haveSize(30)

      val first = list.head
      first.name === "Stallikon Ride"
      first.distance === 23216.8f
    }
  }

}
