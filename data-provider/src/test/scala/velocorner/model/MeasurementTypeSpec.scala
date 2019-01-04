package velocorner.model

import org.specs2.mutable.Specification
import velocorner.model.withings.MeasurementType._

class MeasurementTypeSpec extends Specification {

  "type" should {
    "have proper mapping" in {
      Weight.name === "Weight"
    }
  }
}
