package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views.{ViewDesign, DesignDocument}
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.collection.JavaConversions._


class CouchbaseStorage(password: String) extends Storage {

  lazy val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "velocorner", password)
  val dailyProgressName = "daily_progress"

  override def store(activities: List[Activity]) {
    activities.foreach{a =>
      client.set(a.id.toString, 0, JsonIo.write(a))
    }
  }

  // initializes any connections, pools, resources needed to open a storage session, creates the design documents
  override def initialize() {
    client.deleteDesignDoc(dailyProgressName)
    val dailyProgress = new DesignDocument(dailyProgressName)
    val mapProgress =
      """
        |function (doc, meta) {
        |  if (doc.type == "Ride" && doc.start_date && doc.distance) {
        |    var d = dateToArray(doc.start_date)
        |    emit([d[0], d[1], d[2]],
        |         {
        |           distance: doc.distance,
        |           elevation: doc.total_elevation_gain,
        |           time: doc.moving_time
        |         });
        |  }
        |}
      """.stripMargin
    val viewDesign = new ViewDesign("by_day", mapProgress);
    dailyProgress.getViews().add(viewDesign);
    client.createDesignDoc(dailyProgress)
  }

  // releases any connections, resources used
  override def destroy() {
    client.shutdown(3, TimeUnit.SECONDS)
  }
}
