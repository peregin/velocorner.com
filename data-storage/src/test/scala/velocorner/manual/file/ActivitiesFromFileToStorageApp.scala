package velocorner.manual.file

import org.slf4s.Logging
import velocorner.manual.MyMacConfig
import velocorner.model.Activity
import velocorner.storage.Storage
import velocorner.util.JsonIo

import scala.io.Source


object ActivitiesFromFileToStorageApp extends App with Logging with MyMacConfig {

  //val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/all.json")
  val json = Source.fromURL(getClass.getResource("/data/strava/last10activities.json")).mkString
  val list = JsonIo.read[List[Activity]](json)
  log.info(s"found ${list.size} activities")

  val storage = Storage.create("re") // re or co
  storage.initialize()
  storage.store(list)
  storage.destroy()
}
