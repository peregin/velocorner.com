package velocorner.util

import java.util.zip.GZIPInputStream

import play.api.libs.json._

import scala.io.Source

/**
 * Created by levi on 08/02/15.
 */
object JsonIo {

  def readFromGzipResource[T](resourceName: String)(implicit fjs: Reads[T]): T = {
    val in = new GZIPInputStream(getClass.getResourceAsStream(resourceName))
    val raw = Source.fromInputStream(in).mkString
    read(raw)
  }

  def readStringFromResource(resourceName: String): String = Source.fromURL(getClass.getResource(resourceName)).mkString

  def readReadFromResource[T](resourceName: String)(implicit fjs: Reads[T]): T = {
    val json = readStringFromResource(resourceName)
    read[T](json)
  }

  def readFromFile[T](fileName: String)(implicit fjs: Reads[T]): T = {
    val json = Source.fromFile(fileName).mkString
    read[T](json)
  }
  
  def read[T](json: String)(implicit fjs: Reads[T]): T = {
    val jsonValue = Json.parse(json)
    jsonValue.validate[T] match {
      case JsSuccess(that, _) => that
      case JsError(errors) => sys.error(s"unable to parse file because $errors from $json")
    }
  }

  def write[T](obj: T)(implicit fjs: Writes[T]): String = {
    val json = Json.toJson(obj)
    Json.prettyPrint(json)
  }
}
