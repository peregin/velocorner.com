package spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

// Implicit conversions, such as methods defined in
// org.apache.spark.rdd.PairRDDFunctions
// (http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.PairRDDFunctions)
import org.apache.spark.SparkContext._


/**
 * The hello world of Spark/MapReduce.
 *
 * Created by levi on 02/02/15.
 */
object WordCount extends App {

  def timed[T](text: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    try {
      body
    } finally {
      val elapsed = System.currentTimeMillis() - mark
      println(s"$text took $elapsed millis")
    }
  }

  val sc = new SparkContext("local", "Word Count (2)")
  try {
    spark(sc)
  } finally {
    sc.stop()
  }

  def spark(sc: SparkContext) {
    val rdd: RDD[String] = sc.textFile("common/src/test/resources/data/kipling.the.book.txt")
    println(s"lines: ${rdd.count()}")

    // convert each line to lower case, creating an RDD.
    val input = rdd.map(line => line.toLowerCase)

    // Cache the RDD in memory for fast, repeated access.
    // You don't have to do this and you shouldn't unless the data IS reused.
    // Otherwise, you'll use RAM inefficiently.
    input.cache()

    // Split on non-alphanumeric sequences of characters.
    val wc = timed("sparkling") {
      input
        .flatMap(line => line.split( """\W+"""))
        .map(word => (word, 1))
        .reduceByKey((count1, count2) => count1 + count2)
    }

    println(s"entries: ${wc.count()}")

    // eventually save the results
    //println(s"Writing output to: $out")
    //wc.saveAsTextFile(out)
  }
}
