package velocorner.spark.app

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import velocorner.spark.LocalSpark

object TopNWord extends App with LocalSpark[Seq[(String, Int)]] {

  val n = 20

  runSpark()

  override def sparkAppName: String = s"Top $n Words"

  override def spark(sc: SparkContext): Seq[(String, Int)] = {
    val rdd: RDD[String] = sc.textFile("data-analytics/src/main/resources/data/kipling-if.txt")
    val input = rdd.map(line => line.toLowerCase)
    input
      .flatMap(line => line.split("""\W+"""))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
      .takeOrdered(n)(Ordering[Int].reverse.on(_._2))
      .toSeq
  }
}
