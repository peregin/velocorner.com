package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

/**
 * Created by levi on 10/07/15.
 */
class AthleteSpec extends Specification {

  "model" should {
    "add type to the json" in {
      val athlete = Athlete(10, 1, Some("levi"), Some("colnago"))
      val json = JsonIo.write(athlete)
      json === "{\n  \"id\" : 10,\n  \"resource_state\" : 1,\n  \"firstname\" : \"levi\",\n  \"lastname\" : \"colnago\",\n  \"type\" : \"Athlete\"\n}"
    }
  }
}
