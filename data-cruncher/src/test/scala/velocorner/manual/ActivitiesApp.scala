package velocorner.manual

import play.api.libs.json._
import velocorner.model.Activity

import scala.io.Source


object ActivitiesApp extends App {

  println("reading files...")

  implicit val activityReads = Json.reads[Activity]

  def read(file: String): List[Activity] = {
    val json = Source.fromFile(file).mkString
    val jsonValue = Json.parse(json)
    jsonValue.validate[List[Activity]] match {
      case JsSuccess(list, _) => list
      case JsError(errors) => sys.error(s"unable to parse file because $errors")
    }
  }

  val result = read("/Users/levi/Downloads/strava/dump1.txt")
  println(s"read ${result.size} entries")
  println(result(0))
}
