package velocorner.spark

import org.apache.spark.{SparkContext, SparkConf}
import org.slf4s.Logging

/**
  * Created by levi on 07/03/16.
  */
object PiMonteCarloApp extends App with Logging {

  val samples = 100000000

  val scConf = new SparkConf()
    .setAppName("Pi Approximation")
    .setMaster("local[*]") // set the master to local
  val sc = new SparkContext(scConf)
  val count = sc.parallelize(1 to samples).map{ i=>
    val x = math.random
    val y = math.random
    if (x*x + y*y < 1) 1 else 0
  }.reduce(_ + _)

  sc.stop()

  val pi = 4.0 * count / samples
  log.info(s"pi approximation is $pi")
}
