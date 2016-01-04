package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model.Club
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage

object ClubActivitiesFromStravaToCouchbaseApp extends App with Logging with MyMacConfig {

  private val config = SecretConfig.load()

  log.info("initializing...")
  val feed = new StravaFeed(None, config)
  val storage = new CouchbaseStorage(config.getBucketPassword)
  storage.initialize()
  log.info("retrieving...")
  val activities = feed.listRecentClubActivities(Club.Velocorner)
  log.info("storing...")
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
