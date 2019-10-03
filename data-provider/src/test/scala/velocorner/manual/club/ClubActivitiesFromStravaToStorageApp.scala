package velocorner.manual.club

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.model.strava.Club
import velocorner.storage.Storage
import velocorner.util.CloseableResource

object ClubActivitiesFromStravaToStorageApp extends App with LazyLogging with CloseableResource with AwaitSupport with MyMacConfig {

  logger.info("initializing...")
  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val storage = Storage.create("co")
    storage.initialize()

    logger.info("retrieving...")
    val activities = await(feed.listRecentClubActivities(Club.Velocorner))
    logger.info("storing...")
    await(storage.storeActivity(activities))
    logger.info("done...")

    storage.destroy()
  }
  HttpFeed.shutdown()
}
