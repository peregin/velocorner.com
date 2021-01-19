package velocorner.util

import org.specs2.mutable.Specification

class DemoActivityUtilsSpec extends Specification {

  "utility" should {

    "should generate a stream of activities" in {
      DemoActivityUtils.generate() must beEmpty
    }
  }
}
