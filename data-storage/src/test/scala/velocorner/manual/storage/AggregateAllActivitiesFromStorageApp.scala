package velocorner.manual.storage

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.storage.Storage
import velocorner.util.Metrics


object AggregateAllActivitiesFromStorageApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val storage = Storage.create("mo")
  storage.initialize()

  val progress = timed("aggregation")(storage.dailyProgressForAll(5))
  progress foreach println

  log.info("done...")
  storage.destroy()
}
