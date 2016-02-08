package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics

object ActivitiesFromCouchbaseApp extends App with Logging with Metrics with MyMacConfig {

  log.info("connecting to couchbase bucket...")
  val storage = new CouchbaseStorage(SecretConfig.load().getBucketPassword)
  storage.initialize()

  val recent = storage.listRecentActivities(432909, 20)
  recent foreach println

  storage.destroy()
}
