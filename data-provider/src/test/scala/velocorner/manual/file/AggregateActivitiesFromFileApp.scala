package velocorner.manual.file

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.util.{JsonIo, Metrics}

object AggregateActivitiesFromFileApp extends App with Metrics with LazyLogging {

  logger.info("reading files...")

  // read the 3 dump files and merge it into one single list
  // val activities = timed("reading files") {
  //  (1 to 3).map(i => s"/Users/levi/Downloads/strava/dump$i.txt").map(JsonIo.readFromFile[List[Activity]]).foldLeft(List[Activity]())(_ ++ _)
  // }
  val activities = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/all.json")
  logger.info(s"read ${activities.size} activities")
  val activityTypes = activities.map(_.`type`).distinct
  logger.info(s"activity types ${activityTypes.mkString(", ")}")
  val cyclingActivities = activities.filter(_.`type` == "Ride")
  logger.info(s"cycling activities ${cyclingActivities.size}")

  logger.info("done...")
}
