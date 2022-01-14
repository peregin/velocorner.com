package velocorner.spark.app

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import velocorner.spark.LocalSpark

object WordCount extends App with LocalSpark[Long] {

  runSpark()

  override def sparkAppName: String = "Word Count"

  def spark(sc: SparkContext) = {
    val rdd: RDD[String] = sc.textFile("data-analytics/src/main/resources/data/kipling-if.txt")
    println(s"lines: ${rdd.count()}")

    // convert each line to lower case, creating an RDD.
    val input = rdd.map(line => line.toLowerCase)

    // Cache the RDD in memory for fast, repeated access.
    // You don't have to do this and you shouldn't unless the data IS reused.
    // Otherwise, you'll use RAM inefficiently.
    input.cache()

    // Split on non-alphanumeric sequences of characters.
    val wc =
      input
        .flatMap(line => line.split("""\W+"""))
        .map(word => (word, 1))
        .reduceByKey(_ + _)

    // eventually save the results
    // println(s"Writing output to: $out")
    // wc.saveAsTextFile(out)
    wc.count()
  }
}
