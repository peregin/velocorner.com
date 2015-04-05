package velocorner.util

import org.slf4s.Logging

/**
 * Created by levi on 07/02/15.
 */
trait Metrics extends Logging {

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try {
      body
    } finally {
      val elapsed = System.currentTimeMillis() - mark
      log.info(s"$text took $elapsed millis")
    }
  }
}
