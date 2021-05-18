package velocorner.model.strava

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.JsonIo

class AthleteSpec extends AnyWordSpec with Matchers {

  "model" should {
    "add type to the json" in {
      val athlete = Athlete(
        10,
        1,
        Some("levi"),
        Some("rider"),
        Some("profile picture url"),
        None,
        Some("Switzerland"),
        bikes = None,
        shoes = None
      )
      val json = JsonIo.write(athlete)
      json ===
        """{
          |  "id" : 10,
          |  "resource_state" : 1,
          |  "firstname" : "levi",
          |  "lastname" : "rider",
          |  "profile_medium" : "profile picture url",
          |  "country" : "Switzerland",
          |  "type" : "Athlete"
          |}""".stripMargin
    }
  }
}
