package controllers

import org.specs2.mutable.Specification

class StartupServiceSpec extends Specification {

  "startup service" should {

    "provide elapsed time" in {
      StartupService.elapsedTimeText(1000L) === "00:00:01"
      StartupService.elapsedTimeText(60 * 1000L) === "00:01:00"
      StartupService.elapsedTimeText(60 * 60 * 1000L) === "01:00:00"
      StartupService.elapsedTimeText(16 * 60 * 60 * 1000L) === "16:00:00"
      StartupService.elapsedTimeText(32 * 60 * 60 * 1000L) === "1 day, 08:00:00"
    }
  }
}
