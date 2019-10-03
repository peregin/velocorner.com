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
      val elapsed = System.currentTimeMillis() - mark
      logger.info(s"$text took $elapsed millis")
    }
  }
}
