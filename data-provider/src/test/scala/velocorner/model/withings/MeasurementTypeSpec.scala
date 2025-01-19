package velocorner.model.withings

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.model.withings.MeasurementType._

class MeasurementTypeSpec extends AnyWordSpec with Matchers {

  "type" should {
    "have proper mapping" in
      Weight.name === "Weight"
  }
}
