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
  val token = config.getApplicationToken
  val clientId = config.getApplicationId
  log.info(s"connecting to strava with token [$token] and clientId[$clientId]...")
  val password = config.getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val feed = new StravaFeed(token, clientId)
  val storage = new CouchbaseStorage(password)
  val activities = feed.recentAthleteActivities
  //val activities = feed.listAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
