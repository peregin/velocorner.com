package velocorner.spark

import velocorner.SecretConfig
import velocorner.manual.MyMacConfig

/**
  * Created by levi on 17/02/16.
  */
object SparkCouchbaseApp extends App with MyMacConfig {

  val conn = CouchbaseConnector(SecretConfig.load())
  //conn.list(Seq("244993130", "225250663")).collect()

  val list = conn.dailyProgressForAthlete(432909, 10).collect()
  //val list = conn.dailyProgressForAll(10).collect()

  conn.stop

  println("--------- by athlete -------------")
  list foreach println
}
