package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.storage.Storage

object ActivitiesFromStravaToStorageApp extends App with Logging with MyMacConfig {

  private val config = SecretConfig.load()
  implicit val feed = new StravaFeed(None, config)

  val storage = Storage.create("co")
  storage.initialize()
  //val activities = StravaFeed.listRecentAthleteActivities
  val activities = StravaFeed.listAllAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
