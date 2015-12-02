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

  val password = config.getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")
  val storage = new CouchbaseStorage(password)
  storage.initialize()
  val activities = feed.recentAthleteActivities
  //val activities = feed.listAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
