package velocorner.manual.file

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.model.Activity
import velocorner.storage.CouchbaseStorage
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object ActivitiesFromFileToCouchbaseApp extends App with Logging with MyMacConfig {

  val password = SecretConfig.load().getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/all.json")
  log.info(s"found ${list.size} activities")

  val storage = new CouchbaseStorage(password)
  storage.initialize()
  storage.store(list)
  storage.destroy()
}
