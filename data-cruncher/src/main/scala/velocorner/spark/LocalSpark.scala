package velocorner.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.slf4s.Logging


trait LocalSpark[T] extends Logging {

  val scConf = new SparkConf()
    .setAppName(sparkAppName)
    .setMaster("local[*]") // set the master to local
    .set("spark.driver.bindAddress", "localhost")
    .set("spark.driver.host", "localhost")
  val sc = new SparkContext(scConf)

  def sparkAppName: String

  def spark(sc: SparkContext): T

  def resulted(result: T): T = {
    log.info("--------------------------------------------------------------")
    log.info(s"resulted $result")
    log.info("--------------------------------------------------------------")
    result
  }

  def runSpark(): T = {
    val result = try {
      spark(sc)
    } finally {
      sc.stop()
    }
    resulted(result)
  }
}