package velocorner.manual

import org.slf4s.Logging
import velocorner.storage.Storage
import velocorner.util.Metrics


object AggregateAthleteActivitiesFromStorageApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val storage = Storage.create("co")
  storage.initialize()
  val progress = timed("aggregation")(storage.dailyProgressForAthlete(432909))
  printAllProgress(progress)

  log.info("done...")
  storage.destroy()
}
