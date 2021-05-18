package controllers.util

import org.joda.time.LocalDate
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HolidayUtilSpec extends AnyWordSpec with Matchers {

  "util" should {

    "calculate Christmas holiday" in {
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-12-15")) mustBe false
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-12-19")) mustBe true
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-01-5")) mustBe true
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-01-09")) mustBe false
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-01-15")) mustBe false
    }
  }

}
