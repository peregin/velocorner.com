package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics


object AggregateActivitiesFromCouchbaseApp extends App with Metrics with Logging with AggregateActivities {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val password = SecretConfig.load().getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val storage = new CouchbaseStorage(password)
  val progress = storage.dailyProgress

  printAllProgress(progress)

  log.info("done...")
  storage.destroy()
}
