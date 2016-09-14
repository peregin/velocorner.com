package velocorner.manual.file

import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.storage.RethinkDbStorage
import velocorner.util.JsonIo

import scala.io.Source


object ActivitiesFromFileToRethinkDbApp extends App with Logging {

  val json = Source.fromURL(getClass.getResource("/data/strava/last10activities.json")).mkString
  val list = JsonIo.read[List[Activity]](json)
  log.info(s"found ${list.size} activities")

  val storage = new RethinkDbStorage
  storage.initialize()
  storage.store(list)
  storage.destroy()
}
