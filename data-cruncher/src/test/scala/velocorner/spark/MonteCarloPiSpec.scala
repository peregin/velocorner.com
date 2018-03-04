package velocorner.spark

import org.apache.spark.SparkContext
import org.specs2.mutable.Specification

class MonteCarloPiSpec extends Specification with LocalSpark[Double] {

  val samples = 100000

  "local spark instance" should {

    "calculate PI" in {
      val pi = runSpark()
      pi must beCloseTo(3.1415, .1)
    }
  }

  override def sparkAppName: String = "Approximate π"

  override def spark(sc: SparkContext): Double = {
    // If a circle of radius R is inscribed inside a square with side length 2R,
    // then the area of the circle will be pi*R^2 and the area of the square will be (2R)^2.
    // So the ratio of the area of the circle to the area of the square will be pi/4.
    val count = sc.parallelize(1 to samples).map{ _ =>
      val x = math.random
      val y = math.random
      if (x*x + y*y < 1) 1 else 0
    }.reduce(_ + _)
    4.0 * count / samples
  }
}
