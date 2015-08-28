package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage

/**
 * Created by levi on 13/04/15.
 */
object ActivitiesFromStravaToCouchbaseApp extends App with Logging {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val token = SecretConfig.load().getApplicationToken
  log.info(s"connecting to strava with token [$token]...")
  val password = SecretConfig.load().getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val feed = new StravaFeed(token)
  val storage = new CouchbaseStorage(password)
  val activities = feed.recentAthleteActivities
  //val activities = feed.listAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
