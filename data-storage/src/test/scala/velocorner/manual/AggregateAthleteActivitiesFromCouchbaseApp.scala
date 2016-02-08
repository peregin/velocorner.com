package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics


object AggregateAthleteActivitiesFromCouchbaseApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val password = SecretConfig.load().getBucketPassword
  log.info("connecting to the couchbase bucket...")

  val storage = new CouchbaseStorage(password)
  storage.initialize()
  val progress = timed("aggregation")(storage.dailyProgressForAthlete(432909))

  printAllProgress(progress)

  log.info("done...")
  storage.destroy()
}
