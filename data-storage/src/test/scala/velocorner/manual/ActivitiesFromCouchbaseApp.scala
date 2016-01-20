package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics

object ActivitiesFromCouchbaseApp extends App with Logging with Metrics with MyMacConfig {

  log.info("connecting to couchbase bucket with password...")
  val storage = new CouchbaseStorage(SecretConfig.load().getBucketPassword)
  storage.initialize()

  val ids = storage.listAllActivityIds
  log.info(s"got ${ids.size} ids")

  val recent = storage.listRecentActivities(None, 10)
  recent foreach println

  storage.destroy()
}
