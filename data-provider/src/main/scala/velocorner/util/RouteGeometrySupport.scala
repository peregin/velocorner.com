package velocorner.util

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import scala.util.Try
import velocorner.api.{ActivityRoute, ActivityRoutePoint}

object RouteGeometrySupport {

  val maxPoints = 1500

  def fromStreams(activityId: Long, json: String): Option[ActivityRoute] = {
    val normalized = normalize(parseStreams(json))
    normalized.headOption.map(_ => ActivityRoute.fromPoints(activityId, "streams", normalized))
  }

  def parseStreams(json: String): List[ActivityRoutePoint] =
    Try(Json.parse(json)).toOption
      .map { payload =>
        val latlngs = extractLatLngData(payload)
        val altitudes = extractDoubleData(payload, "altitude")
        val timestamps = extractIntData(payload, "time")

        latlngs.zipWithIndex.flatMap { case ((lat, lon), index) =>
          Some(
            ActivityRoutePoint(
              lat = lat,
              lon = lon,
              ele = altitudes.lift(index),
              ts = timestamps.lift(index)
            )
          )
        }
      }
      .getOrElse(Nil)

  private def extractLatLngData(payload: JsValue): List[(Double, Double)] =
    extractStreamData(payload, "latlng").collect {
      case JsArray(values) if values.size >= 2 =>
        for {
          lat <- values.headOption.flatMap(_.asOpt[Double])
          lon <- values.lift(1).flatMap(_.asOpt[Double])
        } yield (lat, lon)
    }.flatten

  private def extractDoubleData(payload: JsValue, key: String): List[Double] =
    extractStreamData(payload, key).flatMap(_.asOpt[Double])

  private def extractIntData(payload: JsValue, key: String): List[Int] =
    extractStreamData(payload, key).flatMap(_.asOpt[Int])

  private def extractStreamData(payload: JsValue, key: String): List[JsValue] =
    streamByKey(payload, key)
      .flatMap(stream => (stream \ "data").asOpt[List[JsValue]])
      .getOrElse(Nil)

  private def streamByKey(payload: JsValue, key: String): Option[JsObject] =
    payload match {
      case obj: JsObject =>
        obj.value.get(key).flatMap(_.asOpt[JsObject]).orElse(findStreamInArray(obj, key))
      case array: JsArray =>
        findStreamInValues(array.value.toList, key)
      case _ =>
        None
    }

  private def findStreamInArray(obj: JsObject, key: String): Option[JsObject] =
    obj.value
      .get("streams")
      .collect { case JsArray(values) => values.toList }
      .flatMap(findStreamInValues(_, key))

  private def findStreamInValues(values: List[JsValue], key: String): Option[JsObject] =
    values.collectFirst {
      case stream: JsObject if (stream \ "type").asOpt[String].contains(key) => stream
    }

  def normalize(points: List[ActivityRoutePoint], limit: Int = maxPoints): List[ActivityRoutePoint] =
    if (points.size <= limit) points
    else {
      val lastIndex = points.size - 1
      (0 until limit)
        .map(i => math.round(i.toDouble * lastIndex / (limit - 1)).toInt)
        .distinct
        .map(points)
        .toList
    }
}
