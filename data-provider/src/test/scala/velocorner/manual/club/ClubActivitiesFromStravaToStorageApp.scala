package velocorner.manual.club

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.model.Club
import velocorner.feed.StravaActivityFeed
import velocorner.storage.Storage

object ClubActivitiesFromStravaToStorageApp extends App with Logging with MyMacConfig {


  log.info("initializing...")
  private val config = SecretConfig.load()
  val feed = new StravaActivityFeed(None, config)
  val storage = Storage.create("co")
  storage.initialize()

  log.info("retrieving...")
  val activities = feed.listRecentClubActivities(Club.Velocorner)
  log.info("storing...")
  storage.store(activities)
  log.info("done...")

  storage.destroy()
  feed.close()

  sys.exit(0)
}
