package velocorner.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DemoActivityUtilsSpec extends AnyWordSpec with Matchers {

  "utility" should {

    "should generate a stream of activities" in {
      DemoActivityUtils.generate() must not be empty
    }
  }
}
