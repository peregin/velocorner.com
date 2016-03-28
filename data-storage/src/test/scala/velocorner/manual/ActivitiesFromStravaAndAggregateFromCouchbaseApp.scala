package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics

/**
 * Created by levi on 13/04/15.
 */
object ActivitiesFromStravaAndAggregateFromCouchbaseApp extends App with Logging with Metrics with AggregateActivities with MyMacConfig {

  private val config = SecretConfig.load()

  implicit val feed = new StravaFeed(None, config)

  log.info("connecting to couchbase bucket...")
  val storage = new CouchbaseStorage(config.getBucketPassword)
  storage.initialize()
  val activities = StravaFeed.listRecentAthleteActivities
  storage.store(activities)

  val progress = timed("aggregation")(storage.dailyProgressForAthlete(432909))
  printAllProgress(progress)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
