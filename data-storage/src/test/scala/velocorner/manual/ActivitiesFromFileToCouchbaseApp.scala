package velocorner.manual

import com.typesafe.config.ConfigFactory
import velocorner.model.Activity
import velocorner.storage.CouchbaseStorage
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object ActivitiesFromFileToCouchbaseApp extends App {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"
  val config = ConfigFactory.load()
  val password = config.getString("couchbase.bucket.password")
  println(s"connecting to couchbase bucket with password [$password]...")

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/club_test.json")
  println(s"found ${list.size} activities")

  val storage = new CouchbaseStorage(password)
  storage.initialize()
  storage.storeClub(list.sortBy(_.id).slice(0, 1))
  storage.destroy()
}
