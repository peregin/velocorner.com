package velocorner.util

import com.typesafe.scalalogging.LazyLogging
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

import scala.concurrent.{ExecutionContext, Future}

// for kestrel combinator and unsafeTap
import mouse.all._

object Metrics {
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

  def elapsedTimeText(durationInMillis: Long): String = formatter.print(new Period(durationInMillis).normalizedStandard())
}

trait Metrics extends LazyLogging {

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try
      body
    finally
      log(text, mark)
  }

  def timedFuture[T](text: => String)(body: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val mark = System.currentTimeMillis()
    body <| (_.onComplete(_ => log(text, mark)))
  }

  protected def log(text: String, mark: Long): Unit = logger.info(message(text, mark))

  protected def message(text: String, mark: Long): String = {
    val elapsed = System.currentTimeMillis() - mark
    s"$text took \u001b[33m$elapsed millis\u001b[0m"
  }
}
