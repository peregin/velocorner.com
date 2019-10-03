package velocorner.manual.storage

import velocorner.manual.{AggregateActivities, AwaitSupport, MyMacConfig}
import velocorner.storage.Storage
import velocorner.util.Metrics


object ActivitiesFromStorageApp extends App with AggregateActivities  with Metrics with AwaitSupport with MyMacConfig {

  val storage = Storage.create("or") // mo
  storage.initialize()

  val progress = timed("aggregation")(await(storage.dailyProgressForAthlete(432909)))
  printAllProgress(progress)

  storage.destroy()
}
