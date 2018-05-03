package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

/**
 * Created by levi on 10/07/15.
 */
class AthleteSpec extends Specification {

  "model" should {
    "add type to the json" in {
      val athlete = Athlete(
        10, 1, Some("levi"), Some("rider"), Some("profile picture url"), None, Some("Switzerland")
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
