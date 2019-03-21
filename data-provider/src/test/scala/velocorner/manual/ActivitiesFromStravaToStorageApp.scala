package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.storage.Storage

object AthleteFromStravaToStorageApp extends App with Logging with AwaitSupport with MyMacConfig {

  private val config = SecretConfig.load()
  implicit val feed = new StravaActivityFeed(None, config)

  val storage = Storage.create("or")
  storage.initialize()
  //val activities = await(StravaFeed.listRecentAthleteActivities)
  val activities = await(StravaActivityFeed.listAllAthleteActivities)
  await(storage.storeActivity(activities))

  log.info("done...")
  storage.destroy()
  feed.close()
  HttpFeed.shutdown()
}
