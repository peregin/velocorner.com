package velocorner.manual.storage

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, AwaitSupport, MyMacConfig}
import velocorner.storage.Storage
import velocorner.util.Metrics


object ActivitiesFromStorageApp extends App with AggregateActivities with Logging with Metrics with AwaitSupport with MyMacConfig {

  val storage = Storage.create("or") // mo
  storage.initialize()

  //val recent = storage.listRecentActivities(432909, 20)
  //recent foreach println

  val progress = timed("aggregation")(await(storage.dailyProgressForAthlete(432909)))
  printAllProgress(progress)

  storage.destroy()
}
