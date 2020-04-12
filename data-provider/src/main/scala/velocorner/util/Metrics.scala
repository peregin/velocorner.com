package velocorner.util

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

// for kestrel combinator and unsafeTap
import mouse.all._

/**
 * Created by levi on 07/02/15.
 */
trait Metrics extends LazyLogging {

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try {
      body
    } finally {
      log(text, mark)
    }
  }

  def timedFuture[T](text: => String)(body: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val mark =  System.currentTimeMillis()
    body <| (_.onComplete(_ => log(text, mark)))
  }

  protected def log(text: String, mark: Long): Unit = logger.info(message(text, mark))

  protected def message(text: String, mark: Long): String = {
    val elapsed = System.currentTimeMillis() - mark
    s"$text took $elapsed millis"
  }
}
