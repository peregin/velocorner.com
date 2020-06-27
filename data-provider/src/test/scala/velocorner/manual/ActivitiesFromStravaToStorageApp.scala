package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.storage.Storage

object ActivitiesFromStravaToStorageApp extends App with LazyLogging with AwaitSupport with MyLocalConfig {

  private val config = SecretConfig.load()
  private implicit val feed = new StravaActivityFeed(None, config)

  val storage = Storage.create("or")
  storage.initialize()
  //val activities = awaitOn(StravaActivityFeed.listRecentAthleteActivities)
  val activities = awaitOn(StravaActivityFeed.listAllAthleteActivities)
  logger.info(s"found ${activities.size} activities")
  awaitOn(storage.storeActivity(activities))

  logger.info("done...")
  storage.destroy()
  feed.close()
  HttpFeed.shutdown()
}
