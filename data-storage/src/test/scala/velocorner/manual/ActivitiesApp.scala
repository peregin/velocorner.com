package velocorner.manual

import java.time.LocalDate

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.Activity
import velocorner.util.Metrics

import scala.io.Source


object ActivitiesApp extends App with Metrics {

  println("reading files...")

  val dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  implicit val dateTimeFormat = Format[DateTime](Reads.jodaDateReads(dateTimePattern), Writes.jodaDateWrites(dateTimePattern))
  implicit val activityReads = Format[Activity](Json.reads[Activity], Json.writes[Activity])

  def read(file: String): List[Activity] = {
    val json = Source.fromFile(file).mkString
    val jsonValue = Json.parse(json)
    jsonValue.validate[List[Activity]] match {
      case JsSuccess(list, _) => list
      case JsError(errors) => sys.error(s"unable to parse file because $errors")
    }
  }

  // read the 3 dump files and merge it into one single list
  val activities = timed("reading files") {
    (1 to 3).map(i => s"/Users/levi/Downloads/strava/dump$i.txt").map(read).foldLeft(List[Activity]())(_ ++ _)
  }
  println(s"read ${activities.size} activities")
  val activityTypes = activities.map(_.`type`).distinct
  println(s"activity types ${activityTypes.mkString(", ")}")
  val cyclingActivities = activities.filter(_.`type` == "Ride")
  println(s"cycling activities ${cyclingActivities.size}")

  def print(list: List[Activity]) {
    // group by year
    val byYear = list.groupBy(_.start_date_local.year().get())
    // total km in each year
    val yearWithDistance = byYear.map { case (year, list) => (year, list.map(_.distance).sum / 1000) }.toList.sortBy(_._1)
    yearWithDistance.foreach(e => println(f"year ${e._1} -> ${e._2}%.2f"))
  }

  println("Total")
  print(cyclingActivities)


  // each until current day
  val now = LocalDate.now()
  val mn = now.getMonthValue
  val dn = now.getDayOfMonth
  val cyclingActivitiesUntilThisDay = cyclingActivities.filter{a =>
    val m = a.start_date_local.monthOfYear().get()
    val d = a.start_date_local.dayOfMonth().get()
    if (m < mn) true
    else if (m == mn) d <= dn
    else false
  }

  println("Until this day")
  print(cyclingActivitiesUntilThisDay)
}
