package velocorner.manual.cleanup

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics


object NukeAllFromCouchbaseApp extends App with Logging with Metrics with MyMacConfig {

  log.info("connecting to couchbase bucket...")
  val storage = new CouchbaseStorage(SecretConfig.load().getBucketPassword)
  storage.initialize()

  val future = storage.client.flush()

  storage.destroy()
}
