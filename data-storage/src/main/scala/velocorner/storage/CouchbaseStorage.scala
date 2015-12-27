package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views._
import org.slf4s.Logging
import velocorner.model.{Account, DailyProgress, Progress, Activity}
import velocorner.util.JsonIo

import scala.collection.JavaConversions._


class CouchbaseStorage(password: String) extends Storage with Logging {

  lazy val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "velocorner", password)

  val progressDesignName = "progress"
  val byDayViewName = "by_day"

  val listDesignName = "list"
  val activitiesViewName = "activities"
  val accountsViewName = "accounts"


  // activities
  override def store(activities: List[Activity]) {
    activities.foreach{a =>
      client.set(a.id.toString, 0, JsonIo.write(a))
    }
  }

  override def dailyProgress: List[DailyProgress] = progress(daily = true, (entry) => DailyProgress.fromStorage(entry.getKey, entry.getValue))

  override def overallProgress: List[Progress] = progress(daily = false, (entry) => Progress.fromStorage(entry.getValue))

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

  override def listActivityIds: List[Int] = queryForIds(client.getView(listDesignName, activitiesViewName)).map(_.toInt)

  override def deleteActivities(ids: Iterable[Int]) {
    ids.map(_.toString).foreach(client.delete)
  }


  // accounts
  override def store(account: Account) {
    client.set(s"account_${account.athleteId.toString}", 0, JsonIo.write(account))
  }

  override def getAccount(id: Long): Option[Account] = {
    Option(client.get(s"account_$id")).map(json => JsonIo.read[Account](json.toString))
  }

  override def listAccountIds: List[String] = queryForIds(client.getView(listDesignName, accountsViewName))

  private def queryForIds(view: View): List[String] = {
    val query = new Query()
    query.setStale(Stale.FALSE)
    val response = client.query(view, query)
    val ids = for (entry <- response) yield entry.getId
    ids.toList
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
    progressDesign.getViews.add(byDayView)
    client.createDesignDoc(progressDesign)

    client.deleteDesignDoc(listDesignName)
    val listDesign = new DesignDocument(listDesignName)
    listDesign.getViews.add(new ViewDesign(activitiesViewName, mapFunctionForType("Ride")))
    listDesign.getViews.add(new ViewDesign(accountsViewName, mapFunctionForType("Account")))
    client.createDesignDoc(listDesign)
  }

  // releases any connections, resources used
  override def destroy() {
    client.shutdown(1, TimeUnit.SECONDS)
  }

  private def mapFunctionForType(docType: String) = {
    s"""
      |function (doc, meta) {
      |  if (doc.type && doc.type == "$docType") {
      |    emit(meta.id, null);
      |  }
      |}
    """.stripMargin
  }
}
