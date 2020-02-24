package velocorner.util

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by levi on 07/02/15.
 */
trait Metrics extends LazyLogging {

  def timedFuture[T](text: => String)(fut: Future[T]): Future[T] = {
    val mark = System.currentTimeMillis()
    fut.onComplete(_ => log(text, mark))
    fut
  }

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try {
      body
    } finally {
      log(text, mark)
    }
  }

  private def log(text: String, mark: Long): Unit = {
    val elapsed = System.currentTimeMillis() - mark
    logger.info(s"$text took $elapsed millis")
  }
}
