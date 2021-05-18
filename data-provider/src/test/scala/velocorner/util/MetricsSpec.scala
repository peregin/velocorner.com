package velocorner.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MetricsSpec extends AnyWordSpec with Matchers {

  "startup service" should {

    "provide elapsed time" in {
      Metrics.elapsedTimeText(1000L) === "00:00:01"
      Metrics.elapsedTimeText(60 * 1000L) === "00:01:00"
      Metrics.elapsedTimeText(60 * 60 * 1000L) === "01:00:00"
      Metrics.elapsedTimeText(16 * 60 * 60 * 1000L) === "16:00:00"
      Metrics.elapsedTimeText(32 * 60 * 60 * 1000L) === "1 day, 08:00:00"
    }
  }
}
