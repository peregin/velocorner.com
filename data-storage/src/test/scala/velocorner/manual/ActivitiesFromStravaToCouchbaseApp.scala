package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage

/**
 * Created by levi on 13/04/15.
 */
object ActivitiesFromStravaToCouchbaseApp extends App with Logging with MyMacConfig {

  private val config = SecretConfig.load()

  val feed = new StravaFeed(None, config)

  log.info("connecting to couchbase bucket...")
  val storage = new CouchbaseStorage(config.getBucketPassword)
  storage.initialize()
  val activities = feed.listRecentAthleteActivities // 432909
  //val activities = feed.listAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
