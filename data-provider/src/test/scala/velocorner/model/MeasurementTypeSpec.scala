package velocorner.model

import org.specs2.mutable.Specification

class MeasurementTypeSpec extends Specification {

  "type" should {
    "have proper mapping" in {
      Weight.name === "Weight"
    }
  }
}
