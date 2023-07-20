package velocorner.spark.app

import org.apache.spark.SparkContext
import velocorner.spark.LocalSpark

object MonteCarloPi extends App with LocalSpark[Double] {

  val samples = 2000000

  val pi = runSpark()
  println(s"π ~ $pi")

  override def sparkAppName: String = s"Approximate π ${math.Pi}"

  override def spark(sc: SparkContext): Double = {
    // If a circle of radius R is inscribed inside a square with side length 2R,
    // then the area of the circle will be π*R^2 and the area of the square will be (2R)^2.
    // So the ratio of the area of the circle to the area of the square will be π/4.
    val count = sc
      .parallelize(1 to samples)
      .map { _ =>
        val x = math.random()
        val y = math.random()
        if (x * x + y * y < 1) 1 else 0
      }
      .reduce(_ + _)
    4.0 * count / samples
  }
}
