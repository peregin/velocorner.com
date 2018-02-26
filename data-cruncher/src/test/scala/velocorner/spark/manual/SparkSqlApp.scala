package velocorner.spark.manual

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import velocorner.spark.LocalSpark

object SparkSqlApp extends App with LocalSpark[String] {

  runSpark()

  override def sparkAppName = "SQL Activities"

  override def spark(sc: SparkContext) = {
    val ss = SparkSession.builder().config(sc.getConf).getOrCreate()
    val sql = ss.sqlContext

    val df = sql.read.json("data-provider/src/test/resources/data/strava/last30activities.json").toDF()
    df.createOrReplaceTempView("activity")
    df.printSchema()

    val activities = sql.sql("select name from activity")
    activities.show()

    ss.close()
    "done"
  }
}
