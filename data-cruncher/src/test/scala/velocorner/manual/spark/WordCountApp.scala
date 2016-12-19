package velocorner.manual.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import velocorner.util.Metrics


/**
 * The hello world of Spark/MapReduce.
 *
 * Created by levi on 02/02/15.
 */
object WordCountApp extends App with Metrics {

  val scConf = new SparkConf()
    .setAppName("Word Count")
    .setMaster("local[*]") // set the master to local
  val sc = new SparkContext(scConf)
  val resulted = try {
    spark(sc)
  } finally {
    sc.stop()
  }

  println(s"resulted: $resulted")

  def spark(sc: SparkContext) = {
    val rdd: RDD[String] = sc.textFile("data-cruncher/src/test/resources/data/book/kipling.txt")
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
        .reduceByKey(_ + _)
    }

    // eventually save the results
    //println(s"Writing output to: $out")
    //wc.saveAsTextFile(out)
    wc.count()
  }
}

// Exercise: Use other versions of the Bible:
//   The data directory contains similar files for the Tanach (t3utf.dat - in Hebrew),
//   the Latin Vulgate (vuldat.txt), the Septuagint (sept.txt - Greek)
// Exercise: See the Scaladoc page for `OrderedRDDFunctions`:
//   http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.OrderedRDDFunctions
//   Sort the output by word, try both ascending and descending.
//   Note this can be expensive for large data sets!
// Exercise: Take the output from the previous exercise and count the number
//   of words that start with each letter of the alphabet and each digit.
// Exercise (Hard): Sort the output by count. You can't use the same
//   approach as in the previous exercise. Hint: See RDD.keyBy
//   (http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)
//   What's the most frequent word that isn't a "stop word".
// Exercise (Hard): Group the word-count pairs by count. In other words,
//   All pairs where the count is 1 are together (i.e., just one occurrence
//   of those words was found), all pairs where the count is 2, etc. Sort
//   ascending or descending. Hint: Is there a method for grouping?
// Exercise (Thought Experiment): Consider the size of each group created
//   in the previous exercise and the distribution of those sizes vs. counts.
//   What characteristics would you expect for this distribution? That is,
//   which words (or kinds of words) would you expect to occur most
//   frequently? What kind of distribution fits the counts (numbers)?
