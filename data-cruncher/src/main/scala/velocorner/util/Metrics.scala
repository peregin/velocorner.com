package velocorner.util

/**
 * Created by levi on 07/02/15.
 */
trait Metrics {

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try {
      body
    } finally {
      val elapsed = System.currentTimeMillis() - mark
      println(s"$text took $elapsed millis")
    }
  }
}
