package velocorner.manual.file

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.model.strava.Activity
import velocorner.storage.Storage
import velocorner.util.{JsonIo, Metrics}

object ActivitiesFromFileToStorageApp extends App with AggregateActivities with Metrics with Logging with MyMacConfig {

  val list = JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz")
  log.info(s"found ${list.size} activities")

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  log.info("initialized...")

  storage.store(list)
  log.info(s" ${list.size} documents persisted...")
  val progress = timed("aggregation")(storage.dailyProgressForAthlete(432909))
  printAllProgress(progress)

  storage.destroy()
}
