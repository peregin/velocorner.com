package velocorner.manual.cleanup

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics


object NukeALlFromCouchbaseApp extends App with Logging with Metrics with MyMacConfig {

  log.info("connecting to couchbase bucket...")
  val storage = new CouchbaseStorage(SecretConfig.load().getBucketPassword)
  storage.initialize()

  val ids = storage.listAllAccountIds
  log.info(s"accounts ${ids.size}")

  val accs = storage.listAllActivityIds
  log.info(s"activities ${accs.size}")
  storage.deleteActivities(accs)

  storage.destroy()
}
