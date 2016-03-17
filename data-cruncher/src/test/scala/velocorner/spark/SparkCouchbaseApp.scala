package velocorner.spark

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.view.ViewQuery
import org.apache.spark.{SparkContext, SparkConf}
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig

/**
  * Created by levi on 17/02/16.
  */
object SparkCouchbaseApp extends App with MyMacConfig {

  val config = SecretConfig.load()

  val scConf = new SparkConf()
    .setAppName("ActivitiesCb")
    .setMaster("local[*]") // set the master to local
    .set("com.couchbase.bucket.velocorner", config.getBucketPassword) // open the bucket
  val sc = new SparkContext(scConf)

  import com.couchbase.spark._
  sc.couchbaseGet[JsonDocument](Seq("244993130", "225250663"))
    .collect()
    .foreach(println)

  sc.couchbaseView(ViewQuery.from("list", "all_activities_by_date").limit(10))
    .map(_.value)
    .collect()
    .foreach(println)

  sc.stop()
}
