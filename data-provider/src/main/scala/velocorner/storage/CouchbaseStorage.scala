package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views._
import org.slf4s.Logging
import velocorner.model._
import velocorner.util.{JsonIo, Metrics}

import scala.collection.JavaConverters._
import CouchbaseStorage._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.{SunriseSunset, WeatherForecast}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

import scalaz._
import Scalaz._
import scalaz.syntax.traverse.ToTraverseOps


class CouchbaseStorage(password: String) extends Storage with Logging with Metrics {

  lazy private val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri).asJava, "velocorner", password)


  // activities
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = {
    // TODO: bulk store
    activities
      .toList
      .traverseU(a => toScalaFuture(client.set(a.id.toString, 0, JsonIo.write(a))))
      .map(_ => ())
  }


  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]] = {
    val query = new Query()
    query.setGroup(true)
    query.setStale(Stale.FALSE)
    query.setInclusiveEnd(true)
    query.setRange(s"[$athleteId, [2000, 1, 1]]", s"[$athleteId, [3000, 12, 31]]")
    for {
      view <- client.asyncGetView(progressDesignName, athleteProgressByDayViewName)
      res <- client.asyncQuery(view, query)
      list = res.asScala
    } yield list
      .map(entry => DailyProgress.fromStorageByIdDay(entry.getKey, entry.getValue))
  }

  override def getActivity(id: Long): Future[Option[Activity]] = ???

  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = {
    val view = client.getView(listDesignName, athleteActivitiesByDateViewName)
    orderedActivitiesInRange(view, s"[$athleteId, [3000, 1, 1]]", s"[$athleteId, [2000, 12, 31]]", limit)
  }

  private def orderedActivitiesInRange(view: View, rangeFrom: String, rangeTo: String, limit: Int): Future[Iterable[Activity]] = {
    val query = new Query()
    query.setStale(Stale.FALSE)
    query.setInclusiveEnd(true)
    query.setDescending(true)
    query.setLimit(limit)
    query.setRange(rangeFrom, rangeTo)
    query.setIncludeDocs(true)
    for {
      res <- client.asyncQuery(view, query)
      list = res.asScala
    } yield list
      .map(entry => JsonIo.read[Activity](entry.getDocument.toString))
  }

  // accounts
  override def store(account: Account): Future[Unit] = {
    client
      .set(s"account_${account.athleteId.toString}", 0, JsonIo.write(account))
      .map(_ => ())
  }

  override def getAccount(id: Long): Future[Option[Account]] = for {
    account <- client.asyncGet(s"account_$id").map(Option(_).map(json => JsonIo.read[Account](json.toString)))
  } yield account

  // athletes
  override def store(athlete: Athlete): Future[Unit] = {
    client
      .set(athlete.id.toString, 0, JsonIo.write(athlete))
      .map(_ => ())
  }

  override def getAthlete(id: Long): Future[Option[Athlete]] = for {
    athlete <- client.asyncGet(id.toString).map(Option(_).map(json => JsonIo.read[Athlete](json.toString)))
  } yield athlete

  // clubs
  override def store(club: Club): Future[Unit] = {
    client
      .set(s"club_${club.id.toString}", 0, JsonIo.write(club))
      .map(_ => ())
  }

  override def getClub(id: Long): Future[Option[Club]] = for {
    club <- client.asyncGet(s"club_$id").map(Option(_).map(json => JsonIo.read[Club](json.toString)))
  } yield club

  // weather
  override def getWeatherStorage(): WeatherStorage = ???

  // attributes
  override def getAttributeStorage(): AttributeStorage = ???

  private def queryForIds(view: View): Future[Iterable[String]] = {
    val query = new Query()
    query.setStale(Stale.FALSE)
    for {
      response <- client.asyncQuery(view, query)
      list = response.asScala
    } yield list.map(_.getId)
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

  override def backup(fileName: String) = ???

  implicit def toScalaFuture[T](javaFuture: java.util.concurrent.Future[T]): Future[T] = Future{ javaFuture.get() }
}


object CouchbaseStorage {

  val progressDesignName = "progress"
  val athleteProgressByDayViewName = "athlete_by_day"
  val allProgressByDayViewName = "all_by_day"

  val listDesignName = "list"
  val allActivitiesByDateViewName = "all_activities_by_date"
  val athleteActivitiesByDateViewName = "athlete_activities_by_date"
}
