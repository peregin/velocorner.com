package velocorner.manual.club

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.strava.Club
import velocorner.storage.Storage
import velocorner.util.CloseableResource

object ClubActivitiesFromStravaToStorageApp extends App with Logging with CloseableResource with MyMacConfig {

  log.info("initializing...")
  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val storage = Storage.create("co")
    storage.initialize()

    log.info("retrieving...")
    val activities = feed.listRecentClubActivities(Club.Velocorner)
    log.info("storing...")
    storage.storeActivity(activities)
    log.info("done...")

    storage.destroy()
  }
  HttpFeed.shutdown()
}
