package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views._
import org.slf4s.Logging
import velocorner.model.{DailyProgress, Progress, Activity}
import velocorner.util.JsonIo

import scala.collection.JavaConversions._


class CouchbaseStorage(password: String) extends Storage with Logging {

  lazy val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "velocorner", password)

  val progressDesignName = "progress"
  val byDayViewName = "by_day"

  val listDesignName = "list"
  val activitiesViewName = "activities"

  override def store(activities: List[Activity]) {
    activities.foreach{a =>
      client.set(a.id.toString, 0, JsonIo.write(a))
    }
  }

  override def dailyProgress: List[DailyProgress] = progress(true, (entry) => DailyProgress.fromStorage(entry.getKey, entry.getValue))

  override def overallProgress: List[Progress] = progress(false, (entry) => Progress.fromStorage(entry.getValue))

  // queries the daily or overall progress
  private def progress[T](daily: Boolean, func: ViewRow => T): List[T] = {
    val view = client.getView(progressDesignName, byDayViewName)
    val query = new Query()
    query.setGroup(daily)
    query.setStale(Stale.FALSE)
    val response = client.query(view, query)
    val progress = for (entry <- response) yield func(entry)
    progress.toList
  }

  override def listActivityIds: List[Int] = {
    val view = client.getView(listDesignName, activitiesViewName)
    val query = new Query()
    query.setStale(Stale.FALSE)
    val response = client.query(view, query)
    val ids = for (entry <- response) yield entry.getId.toInt
    ids.toList
  }

  override def deleteActivities(ids: Iterable[Int]) {
    ids.map(_.toString).foreach(client.delete)
  }

  // initializes any connections, pools, resources needed to open a storage session, creates the design documents
  override def initialize() {
    client.deleteDesignDoc(progressDesignName)
    val progressDesign = new DesignDocument(progressDesignName)
    val mapProgress =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride" && doc.start_date && doc.distance) {
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
    val reduceProgress =
      """
        |function(key, values, rereduce) {
        |  var res = {ride: 0,
        |             dist: 0,
        |             distmax: 0,
        |             elev: 0,
        |             elevmax: 0,
        |             time: 0};
        |  for(i=0; i < values.length; i++) {
        |    if (rereduce) {
        |      res.ride += values[i].ride;
        |      res.dist += values[i].dist;
        |      res.distmax = Math.max(res.distmax, values[i].dist);
        |      res.elev += values[i].elev;
        |      res.elevmax = Math.max(res.elevmax, values[i].elev);
        |      res.time += values[i].time;
        |    } else {
        |      res.ride += 1;
        |      res.dist += values[i].distance;
        |      res.distmax = Math.max(res.distmax, values[i].distance);
        |      res.elev += values[i].elevation;
        |      res.elevmax = Math.max(res.elevmax, values[i].elevation);
        |      res.time += values[i].time;
        |    }
        |  }
        |  return res;
        |}
      """.stripMargin
    val byDayView = new ViewDesign(byDayViewName, mapProgress, reduceProgress)
    progressDesign.getViews().add(byDayView)
    client.createDesignDoc(progressDesign)

    client.deleteDesignDoc(listDesignName)
    val listDesign = new DesignDocument(listDesignName)
    val mapActivities =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride") {
        |    emit(meta.id, null);
        |  }
        |}
      """.stripMargin
    val activitiesView = new ViewDesign(activitiesViewName, mapActivities)
    listDesign.getViews.add(activitiesView)
    client.createDesignDoc(listDesign)
  }

  // releases any connections, resources used
  override def destroy() {
    client.shutdown(1, TimeUnit.SECONDS)
  }
}
