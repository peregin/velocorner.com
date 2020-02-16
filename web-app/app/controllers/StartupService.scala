package controllers

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Singleton
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

object StartupService {

  val startupTime = System.currentTimeMillis()

  val formatter = new PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix(" day,", " days,")
    .appendSeparator(" ")
    .printZeroIfSupported()
    .minimumPrintedDigits(2)
    .appendHours()
    .appendSeparator(":")
    .appendMinutes()
    .printZeroIfSupported()
    .minimumPrintedDigits(2)
    .appendSeparator(":")
    .appendSeconds()
    .minimumPrintedDigits(2)
    .toFormatter()

  def elapsedTimeText(): String = elapsedTimeText(System.currentTimeMillis() - startupTime)

  def elapsedTimeText(duration: Long): String = formatter.print(new Period(duration).normalizedStandard())

}

@Singleton
class StartupService extends LazyLogging {

  logger.info(s"startup service initializing at ${StartupService.startupTime} ...")
}
