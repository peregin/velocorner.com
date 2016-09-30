package velocorner.manual

import org.slf4s.Logging
import velocorner.storage.Storage
import velocorner.util.Metrics


object ActivitiesFromStorageApp extends App with AggregateActivities with Logging with Metrics with MyMacConfig {

  val storage = Storage.create("mo")
  storage.initialize()

  //val recent = storage.listRecentActivities(432909, 20)
  //recent foreach println
  val progress = timed("aggregation")(storage.dailyProgressForAthlete(432909))
  printAllProgress(progress)

  storage.destroy()
}
