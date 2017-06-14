package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

/**
  * Created by levi on 14.06.17.
  */
class ProgressSpec extends Specification {

  val progress = Progress(10, 1200, 1500, 1000, 27.2, 512, 602)

  "model" should {

    "read and write from json" in {
      val json = JsonIo.write(progress)
      json ===
        """{
          |  "rides" : 10,
          |  "distance" : 1200,
          |  "longestDistance" : 1500,
          |  "movingTime" : 1000,
          |  "averageSpeed" : 27.2,
          |  "elevation" : 512,
          |  "longestElevation" : 602
          |}""".stripMargin
    }

    "aggregate" in {
      val progress2 = Progress(2, 1230, 1600, 1100, 28.1, 515, 602)
      progress + progress2 === Progress(12, 2430, 1600, 2100, 28.1, 1027, 602)
    }
  }
}
