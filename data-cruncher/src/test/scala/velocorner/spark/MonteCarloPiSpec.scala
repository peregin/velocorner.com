package velocorner.spark

import org.apache.spark.SparkContext
import org.specs2.mutable.Specification

class MonteCarloPiSpec extends Specification with LocalSpark[Double] {

  val samples = 100000

  "local spark instance" should {

    "calculate PI" in {
      val pi = runSpark()
      pi must beCloseTo(3.1415, .01)
    }
  }

  override def sparkAppName: String = "Approximate π"

  override def spark(sc: SparkContext): Double = {
    val count = sc.parallelize(1 to samples).map{ _ =>
      val x = math.random
      val y = math.random
      if (x*x + y*y < 1) 1 else 0
    }.reduce(_ + _)
    4.0 * count / samples
  }
}
