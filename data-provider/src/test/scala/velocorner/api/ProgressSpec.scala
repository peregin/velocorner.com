package velocorner.api

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.JsonIo

class ProgressSpec extends AnyWordSpec with Matchers {

  private val progress = Progress(
    days = 1,
    rides = 10,
    distance = 1200,
    longestDistance = 1500,
    movingTime = 1000,
    averageSpeed = 27.2,
    elevation = 512,
    longestElevation = 602
  )

  "model" should {

    "read and write from json" in {
      val json = JsonIo.write(progress)
      json ===
        """{
          |  "days" : 1,
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
      val progress2 = Progress(2, 2, 1230, 1600, 1100, 28.1, 515, 602)
      progress + progress2 === Progress(3, 12, 2430, 1600, 2100, 28.1, 1027, 602)
    }

    "convert to imperial" in {
      val imperial = progress.to(Units.Imperial)
      imperial.distance === 745.6454306848008
      imperial.averageSpeed === 16.901296428855485
      imperial.elevation === 1679.7866666733858
    }
  }
}
