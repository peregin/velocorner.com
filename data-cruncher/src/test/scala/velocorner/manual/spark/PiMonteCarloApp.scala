package velocorner.manual.spark

import org.apache.spark.SparkContext
import velocorner.spark.LocalSpark

/**
  * Created by levi on 07/03/16.
  */
object PiMonteCarloApp extends App with LocalSpark[Double] {

  val samples = 10000000

  runSpark()

  override def sparkAppName: String = "Pi Approximation"

  override def spark(sc: SparkContext) = {
    val count = sc.parallelize(1 to samples).map{ _ =>
      val x = math.random
      val y = math.random
      if (x*x + y*y < 1) 1 else 0
    }.reduce(_ + _)

    4.0 * count / samples
  }
}
