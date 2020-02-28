package velocorner.util

import com.typesafe.scalalogging.LazyLogging

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

  protected def message(text: String, mark: Long): String = {
    val elapsed = System.currentTimeMillis() - mark
    s"$text took $elapsed millis"
  }

  protected def log(text: String, mark: Long): Unit = logger.info(message(text, mark))
}
