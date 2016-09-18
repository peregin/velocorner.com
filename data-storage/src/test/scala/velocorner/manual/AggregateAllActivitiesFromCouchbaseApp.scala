package velocorner.manual

import org.slf4s.Logging
import velocorner.storage.Storage
import velocorner.util.Metrics


object AggregateAllActivitiesFromCouchbaseApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val storage = Storage.create("co")
  storage.initialize()

  val progress = timed("aggregation")(storage.dailyProgressForAll(5))
  progress foreach println

  log.info("done...")
  storage.destroy()
}
