package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.storage.Storage
import velocorner.util.Metrics

object ActivitiesFromStravaAndAggregateApp extends App with LazyLogging with Metrics with AggregateActivities with AwaitSupport with MyMacConfig {

  private val config = SecretConfig.load()
  implicit val feed = new StravaActivityFeed(None, config)

  val storage = Storage.create("or")
  storage.initialize()
  val activities = await(StravaActivityFeed.listRecentAthleteActivities)
  logger.info(s"retrieved ${activities.size} activities")
  await(storage.storeActivity(activities))

  val progress = timed("aggregation")(await(storage.dailyProgressForAthlete(432909, "Ride")))
  printAllProgress(progress)

  logger.info("done...")
  storage.destroy()
  feed.close()
  HttpFeed.shutdown()
}
