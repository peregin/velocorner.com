package velocorner.manual

import org.joda.time.LocalDate
import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.util.{JsonIo, Metrics}


object AggregateActivitiesFromFileApp extends App with Metrics with Logging {

  log.info("reading files...")

  // read the 3 dump files and merge it into one single list
  //val activities = timed("reading files") {
  //  (1 to 3).map(i => s"/Users/levi/Downloads/strava/dump$i.txt").map(JsonIo.readFromFile[List[Activity]]).foldLeft(List[Activity]())(_ ++ _)
  //}
  val activities = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/all.json")
  log.info(s"read ${activities.size} activities")
  val activityTypes = activities.map(_.`type`).distinct
  log.info(s"activity types ${activityTypes.mkString(", ")}")
  val cyclingActivities = activities.filter(_.`type` == "Ride")
  log.info(s"cycling activities ${cyclingActivities.size}")

  def print(from: List[Activity]) {
    // group by year
    val byYear = from.groupBy(_.start_date_local.year().get())
    // total km in each year
    val yearWithDistance = byYear.map { case (year, list) => (year, list.map(_.distance).sum / 1000) }.toList.sortBy(_._1)
    yearWithDistance.foreach(e => log.info(f"year ${e._1} -> ${e._2}%.2f km"))
  }

  log.info("Total")
  print(cyclingActivities)


  // each until current day
  val now = LocalDate.now()
  val mn = now.monthOfYear().get()
  val dn = now.getDayOfMonth
  val cyclingActivitiesUntilThisDay = cyclingActivities.filter{a =>
    val m = a.start_date_local.monthOfYear().get()
    val d = a.start_date_local.dayOfMonth().get()
    if (m < mn) true
    else if (m == mn) d <= dn
    else false
  }

  log.info("Until this day")
  print(cyclingActivitiesUntilThisDay)

  log.info("done...")
}
