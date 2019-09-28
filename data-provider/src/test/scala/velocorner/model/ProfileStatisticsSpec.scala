package velocorner.model

import org.joda.time.LocalDate
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class ProfileStatisticsSpec extends Specification {

  val progress = Progress(10, 1200, 1500, 1000, 27.2, 512, 602)

  "model" should {

    "estimate progress" in {
      val statistics = ProfileStatistics.from(50, 100, progress)
      statistics.yearlyPercentile === 50
      statistics.estimate.rides === 20
      statistics.estimate.distance === 2400
      statistics.estimate.longestDistance === 1500
      statistics.estimate.movingTime === 2000
      statistics.estimate.averageSpeed === 27.2
      statistics.estimate.elevation === 1024
      statistics.estimate.longestElevation === 602
    }

    "estimate progress" in {
      val now = LocalDate.parse("2019-09-26")
      val statistics = ProfileStatistics.from(now, progress)
      statistics.yearlyPercentile === 73
      statistics.estimate.rides === 13
      statistics.estimate.distance must beCloseTo(1628.25, .01)
    }

    "read and write from json" in {
      val statistics = ProfileStatistics.from(50, 100, progress)
      val json = JsonIo.write(statistics)
      val read = JsonIo.read[ProfileStatistics](json)
      read === statistics
    }
  }
}
