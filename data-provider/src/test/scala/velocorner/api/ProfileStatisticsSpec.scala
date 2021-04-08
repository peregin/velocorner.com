package velocorner.api

import org.joda.time.LocalDate
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.JsonIo

class ProfileStatisticsSpec extends AnyWordSpec with Matchers {

  private val progress = Progress(2, 10, 1200, 1500, 1000, 27.2, 512, 602)

  "model" should {

    "estimate progress 50%" in {
      val statistics = ProfileStatistics.from(50, 100, progress, progress)
      statistics.yearlyPercentile === 50
      statistics.estimate.rides === 20
      statistics.estimate.distance === 2400d
      statistics.estimate.longestDistance === 1500d
      statistics.estimate.movingTime === 2000L
      statistics.estimate.averageSpeed === 27.2d
      statistics.estimate.elevation === 1024d
      statistics.estimate.longestElevation === 602d
    }

    "estimate progress from given date" in {
      val now = LocalDate.parse("2019-09-26")
      val statistics = ProfileStatistics.from(now, progress, progress)
      statistics.yearlyPercentile === 73
      statistics.estimate.rides === 13
      statistics.estimate.distance mustBe 1628.25 +- .01
    }

    "read and write from json" in {
      val statistics = ProfileStatistics.from(50, 100, progress, progress)
      val json = JsonIo.write(statistics)
      val read = JsonIo.read[ProfileStatistics](json)
      read === statistics
    }
  }
}
