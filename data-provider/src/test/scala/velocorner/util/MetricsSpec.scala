package velocorner.util

import org.specs2.mutable.Specification

class MetricsSpec extends Specification {

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
