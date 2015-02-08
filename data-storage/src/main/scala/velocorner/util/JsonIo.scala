package velocorner.util

import play.api.libs.json.{Reads, JsError, JsSuccess, Json}

import scala.io.Source

/**
 * Created by levi on 08/02/15.
 */
object JsonIo {

  def readFromFile[T](fileName: String)(implicit fjs: Reads[T]): T = {
    val json = Source.fromFile(fileName).mkString
    readFromText(json)
  }
  
  def readFromText[T](json: String)(implicit fjs: Reads[T]): T = {
    val jsonValue = Json.parse(json)
    jsonValue.validate[T] match {
      case JsSuccess(list, _) => list
      case JsError(errors) => sys.error(s"unable to parse file because $errors")
    }
  }
}
