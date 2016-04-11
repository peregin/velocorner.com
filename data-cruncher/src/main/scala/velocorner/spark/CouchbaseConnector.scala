package velocorner.spark

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.view.ViewQuery
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

  import com.couchbase.spark._

  def list(ids: Seq[String]) = sc.couchbaseGet[JsonDocument](ids)

  def dailyProgressForAthlete(athleteId: Int, limit: Int) = sc.couchbaseView(
    ViewQuery.from(CouchbaseStorage.listDesignName, CouchbaseStorage.allActivitiesByDateViewName)
      .limit(limit)
      .descending()
      .inclusiveEnd(true)
      //.startKey(s"[$athleteId, [3000, 1, 1]]")
      //.endKey(s"[$athleteId, [2000, 12, 31]]")
  )

  def dailyProgressForAll(limit: Int) = sc.couchbaseView(ViewQuery.from(CouchbaseStorage.listDesignName, CouchbaseStorage.athleteActivitiesByDateViewName)
    .limit(limit)
    .descending()
    .inclusiveEnd(true)
  )

  def stop = sc.stop()
}
