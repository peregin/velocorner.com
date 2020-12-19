package controllers.util

import org.joda.time.{DateTimeZone, LocalDate}

object HolidayUtil {

  def isWinterHoliday(): Boolean = isWinterHoliday(LocalDate.now(DateTimeZone.UTC))

  def isWinterHoliday(now: LocalDate): Boolean = {
    val day = now.getDayOfYear
    println(s"day is $day")
    day < 7 || day > 350
  }
}
