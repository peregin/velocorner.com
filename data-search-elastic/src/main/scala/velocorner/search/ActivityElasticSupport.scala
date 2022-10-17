package velocorner.search

import cats.Show
import cats.implicits._
import com.sksamuel.elastic4s.ElasticDsl.{createIndex, geopointField, properties}
import com.sksamuel.elastic4s.api.IndexApi
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexRequest, IndexRequest}
import com.sksamuel.elastic4s.requests.searches.GeoPoint
import play.api.libs.json.{JsObject, JsString, Json}
import velocorner.api.strava.Activity
import velocorner.util.JsonIo

trait ActivityElasticSupport extends IndexApi with ElasticSupport {

  val ixName = "activity"

  def setup(): CreateIndexRequest = createIndex(ixName).mapping(properties(geopointField("location")))

  def toIndices(activities: Iterable[Activity]): Iterable[IndexRequest] =
    activities.map { activity =>
      val ixDefinition = indexInto(ixName)
      val ix = ixDefinition.withId(activity.id.toString)

      val convertedActivity = activity.copy(
        max_speed = activity.max_speed.map(_ * 3.6f), // to km/h
        average_speed = activity.average_speed.map(_ * 3.6f) // to km/h
      )

      // enrich with location
      val maybeGeoPoint = for {
        lat <- activity.start_latitude
        lon <- activity.start_longitude
      } yield GeoPoint(lat, lon)
      implicit val gpShow: Show[GeoPoint] = Show.show[GeoPoint](gp => s"${gp.lat},${gp.long}")
      val json = (Json.toJson(convertedActivity), maybeGeoPoint) match {
        case (jsObj: JsObject, Some(geoPoint)) => jsObj + ("location", JsString(geoPoint.show))
        case (jsAny, _)                        => jsAny
      }
      ix.doc(json.toString())
    }

  // specific to elastic bulk upload, doc json must be in one line
  private def extractFullDoc(a: Activity, id: IndexRequest): IndexRequest = id.doc(JsonIo.write(a, pretty = false))

  // only specific fields
  private def extractIndices(a: Activity, id: IndexRequest): IndexRequest = id.fields(
    "name" -> a.name,
    "start_date" -> a.start_date,
    "distance" -> a.distance / 1000,
    "elevation" -> a.total_elevation_gain,
    "average_speed" -> a.average_speed.getOrElse(0f),
    "max_speed" -> a.max_speed.getOrElse(0f),
    "average_temp" -> a.average_temp.getOrElse(0f),
    "average_watts" -> a.average_watts.getOrElse(0f)
  )
}
