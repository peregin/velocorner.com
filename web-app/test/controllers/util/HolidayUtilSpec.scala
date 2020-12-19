package controllers.util

import org.joda.time.LocalDate
import org.specs2.mutable.Specification

class HolidayUtilSpec extends Specification {

  "util" should {
    "calculate Christmas holiday" in {
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-12-15")) should beFalse
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-12-19")) should beTrue
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-01-5")) should beTrue
      HolidayUtil.isWinterHoliday(LocalDate.parse("2020-01-15")) should beFalse
    }
  }

}
