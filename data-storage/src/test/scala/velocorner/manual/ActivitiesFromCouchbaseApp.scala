package velocorner.manual

import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage

/**
 * Created by levi on 04/04/15.
 */
object ActivitiesFromCouchbaseApp extends App {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val password = SecretConfig.getBucketPassword
  println(s"connecting to couchbase bucket with password [$password]...")

  val storage = new CouchbaseStorage(password)
  storage.initialize()

  storage.destroy()
}
