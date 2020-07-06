package velocorner.manual.file

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.manual.{AggregateActivities, AwaitSupport, MyLocalConfig}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import velocorner.util.{JsonIo, Metrics}

object ActivitiesFromFileToStorageApp extends App with AggregateActivities with Metrics with LazyLogging with AwaitSupport with MyLocalConfig {

  val list = JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz")
  logger.info(s"found ${list.size} activities")

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  logger.info("initialized...")

  awaitOn(storage.storeActivity(list))
  logger.info(s" ${list.size} documents persisted...")
  val progress = timed("aggregation")(awaitOn(
    storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
  )
  printAllProgress(progress)

  storage.destroy()
}
