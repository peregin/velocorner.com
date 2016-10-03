package velocorner.manual.club

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.model.Club
import velocorner.proxy.StravaFeed
import velocorner.storage.Storage

object ClubActivitiesFromStravaToStorageApp extends App with Logging with MyMacConfig {


  log.info("initializing...")
  private val config = SecretConfig.load()
  val feed = new StravaFeed(None, config)
  val storage = Storage.create("co")
  storage.initialize()

  log.info("retrieving...")
  val activities = feed.listRecentClubActivities(Club.Velocorner)
  log.info("storing...")
  storage.store(activities)
  log.info("done...")

  storage.destroy()

  sys.exit(0)
}
