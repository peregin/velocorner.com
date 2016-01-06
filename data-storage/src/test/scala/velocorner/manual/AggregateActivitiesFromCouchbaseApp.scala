package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics


object AggregateActivitiesFromCouchbaseApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val password = SecretConfig.load().getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val storage = new CouchbaseStorage(password)
  storage.initialize()
  val progress = timed("aggregation")(storage.dailyProgress(432909))

  printAllProgress(progress)

  log.info("done...")
  storage.destroy()
}
