package velocorner.spark

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.view.ViewQuery
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage

/**
  * Created by levi on 06/04/16.
  */
case class CouchbaseConnector(config: SecretConfig) {

  val scConf = new SparkConf()
    .setAppName("ActivitiesCb")
    .setMaster("local[*]") // set the master to local
    .set("com.couchbase.bucket.velocorner", config.getBucketPassword) // open the bucket
  val sc = new SparkContext(scConf)
  val sql = new SQLContext(sc)

  import com.couchbase.spark._

  def list(ids: Seq[String]) = sc.couchbaseGet[JsonDocument](ids)

  def dailyProgressForAthlete(athleteId: Int, limit: Int) = sql.sql("select id from velocorner limit 10")
    //sc.couchbaseQuery(Query.simple(""))
    // filtering on the view is not working based on start/end key
    // key looks like athleteId, date: [432909,[2014,10,17]]
    //ViewQuery.from(CouchbaseStorage.listDesignName, CouchbaseStorage.athleteActivitiesByDateViewName)
    //  .descending()
  //).filter(_.key.toString.startsWith(s"[$athleteId,"))


  def dailyProgressForAll(limit: Int) = sc.couchbaseView(ViewQuery.from(CouchbaseStorage.listDesignName, CouchbaseStorage.allActivitiesByDateViewName)
    .limit(limit)
    .descending()
    .inclusiveEnd(true)
  )

  def stop = sc.stop()
}
