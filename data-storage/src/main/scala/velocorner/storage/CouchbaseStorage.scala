package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views._
import org.slf4s.Logging
import velocorner.model._
import velocorner.util.{Metrics, JsonIo}

import scala.collection.JavaConversions._


class CouchbaseStorage(password: String) extends Storage with Logging with Metrics {

  lazy val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "velocorner", password)

  val progressDesignName = "progress"
  val athleteProgressByDayViewName = "athlete_by_day"
  val allProgressByDayViewName = "all_by_day"

  val listDesignName = "list"
  val allActivitiesByDateViewName = "all_activities_by_date"
  val athleteActivitiesByDateViewName = "athlete_activities_by_date"


  // activities
  override def store(activities: Iterable[Activity]) {
    // TODO: bulk store
    activities.foreach{a =>
      client.set(a.id.toString, 0, JsonIo.write(a))
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = {
    val view = client.getView(progressDesignName, athleteProgressByDayViewName)
    val query = new Query()
    query.setGroup(true)
    query.setStale(Stale.FALSE)
    query.setInclusiveEnd(true)
    query.setRange(s"[$athleteId, [2000, 1, 1]]", s"[$athleteId, [3000, 12, 31]]")
    val response = client.query(view, query)
    for (entry <- response) yield DailyProgress.fromStorageByIdDay(entry.getKey, entry.getValue)
  }

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = {
    val view = client.getView(progressDesignName, allProgressByDayViewName)
    val query = new Query()
    query.setGroup(true)
    query.setStale(Stale.FALSE)
    query.setInclusiveEnd(true)
    query.setLimit(limit)
    query.setDescending(true)
    val response = client.query(view, query)
    for (entry <- response) yield AthleteDailyProgress.fromStorageByDateId(entry.getKey, entry.getValue)
  }

  override def listRecentActivities(limit: Int): Iterable[Activity] = {
    val view = client.getView(listDesignName, allActivitiesByDateViewName)
    orderedActivitiesInRange(view, "[3000, 1, 1]", "[2000, 12, 31]", limit)
  }

  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = {
    val view = client.getView(listDesignName, athleteActivitiesByDateViewName)
    orderedActivitiesInRange(view, s"[$athleteId, [3000, 1, 1]]", s"[$athleteId, [2000, 12, 31]]", limit)
  }

  private def orderedActivitiesInRange(view: View, rangeFrom: String, rangeTo: String, limit: Int): Iterable[Activity] = {
    val query = new Query()
    query.setStale(Stale.FALSE)
    query.setInclusiveEnd(true)
    query.setDescending(true)
    query.setLimit(limit)
    query.setRange(rangeFrom, rangeTo)
    query.setIncludeDocs(true)
    val response = client.query(view, query)
    for (entry <- response) yield JsonIo.read[Activity](entry.getDocument.toString)
  }

  // accounts
  override def store(account: Account) {
    client.set(s"account_${account.athleteId.toString}", 0, JsonIo.write(account))
  }

  override def getAccount(id: Long): Option[Account] = {
    Option(client.get(s"account_$id")).map(json => JsonIo.read[Account](json.toString))
  }

  private def queryForIds(view: View): Iterable[String] = {
    val query = new Query()
    query.setStale(Stale.FALSE)
    val response = client.query(view, query)
    for (entry <- response) yield entry.getId
  }

  // initializes any connections, pools, resources needed to open a storage session, creates the design documents
  override def initialize() = timed("init") {
    client.deleteDesignDoc(progressDesignName)
    val progressDesign = new DesignDocument(progressDesignName)
    val mapAthleteDateProgress =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride" && doc.start_date && doc.athlete && doc.distance) {
        |    var d = dateToArray(doc.start_date)
        |    emit([doc.athlete.id, [d[0], d[1], d[2]]],
        |         {
        |           distance: doc.distance,
        |           elevation: doc.total_elevation_gain,
        |           time: doc.moving_time
        |         });
        |  }
        |}
      """.stripMargin
    val mapAllDateProgress =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride" && doc.start_date && doc.athlete && doc.distance) {
        |    var d = dateToArray(doc.start_date)
        |    emit([[d[0], d[1], d[2]], doc.athlete.id],
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
    val athleteByDayView = new ViewDesign(athleteProgressByDayViewName, mapAthleteDateProgress, reduceProgress)
    progressDesign.getViews.add(athleteByDayView)
    val allByDayView = new ViewDesign(allProgressByDayViewName, mapAllDateProgress, reduceProgress)
    progressDesign.getViews.add(allByDayView)
    client.createDesignDoc(progressDesign)

    client.deleteDesignDoc(listDesignName)
    val listDesign = new DesignDocument(listDesignName)
    val mapAllActivitiesByDate =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride") {
        |    var d = dateToArray(doc.start_date)
        |    emit([d[0], d[1], d[2]], doc.id);
        |  }
        |}
      """.stripMargin
    listDesign.getViews.add(new ViewDesign(allActivitiesByDateViewName, mapAllActivitiesByDate))
    val mapAthleteActivitiesByDate =
      """
        |function (doc, meta) {
        |  if (doc.type && doc.type == "Ride") {
        |    var d = dateToArray(doc.start_date)
        |    emit([doc.athlete.id, [d[0], d[1], d[2]]], doc.id);
        |  }
        |}
      """.stripMargin
    listDesign.getViews.add(new ViewDesign(athleteActivitiesByDateViewName, mapAthleteActivitiesByDate))
    client.createDesignDoc(listDesign)
  }

  // releases any connections, resources used
  override def destroy() {
    client.shutdown(1, TimeUnit.SECONDS)
  }
}
