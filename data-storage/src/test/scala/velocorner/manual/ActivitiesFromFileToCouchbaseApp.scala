package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model.Activity
import velocorner.storage.CouchbaseStorage
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object ActivitiesFromFileToCouchbaseApp extends App with Logging {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val password = SecretConfig.getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/all.json")
  log.info(s"found ${list.size} activities")

  val storage = new CouchbaseStorage(password)
  storage.initialize()
  storage.store(list)
  storage.destroy()
}
