package velocorner.storage

import velocorner.model._
import velocorner.util.JsonIo
import MongoDbStorage._
import com.mongodb.{DBCursor, DBObject}
import com.mongodb.casbah.{MongoClient, MongoDB}
import com.mongodb.util.JSON
import com.mongodb.casbah.query.Imports._
import org.slf4s.Logging
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.{SunriseSunset, WeatherForecast}

import scala.language.implicitConversions
import collection.JavaConverters._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by levi on 28/09/16.
  * Access layer to the MongoDb.
  * TODO: use bulk upsert
  * TODO: use async API
  */
class MongoDbStorage extends Storage with Logging {

  lazy val client = MongoClient()
  @volatile var db: Option[MongoDB] = None


  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = Future {
    val coll = db.getCollection(ACTIVITY_TABLE)
    activities.foreach{ a =>
      val json = JsonIo.write(a)
      val upd = "id" $eq a.id
      coll.update(upd, json, true, false)
    }
  }

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]] = Future {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = $and("athlete.id" $eq athleteId, "type" $eq "Ride")
    val results = coll.find(query)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
  }

  override def getActivity(id: Long): Future[Option[Activity]] = getJsonById(id, ACTIVITY_TABLE, "id").map(_.map(JsonIo.read[Activity]))

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = Future {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = $and("athlete.id" $eq athleteId, "type" $eq "Ride")
    val results = coll.find(query).sort("{start_date:-1}").limit(limit)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found recent activities ${activities.size} for $athleteId")
    activities
  }

  private def upsert(json: String, id: Long, collName: String, idName: String = "id"): Future[Unit] = Future {
    val coll = db.getCollection(collName)
    val upd = idName $eq id
    coll.update(upd, json, true, false)
  }

  private def getJsonById(id: Long, collName: String, idName: String = "id"): Future[Option[String]] = Future {
    val coll = db.getCollection(collName)
    val query = idName $eq id
    coll.find(query).headOption
  }

  // accounts
  override def store(account: Account): Future[Unit] = upsert(JsonIo.write(account), account.athleteId, ACCOUNT_TABLE, "athleteId")

  override def getAccount(id: Long): Future[Option[Account]] = getJsonById(id, ACCOUNT_TABLE, "athleteId").map(_.map(JsonIo.read[Account]))

  // athletes
  override def store(athlete: Athlete): Future[Unit] = upsert(JsonIo.write(athlete), athlete.id, ATHLETE_TABLE)

  override def getAthlete(id: Long): Future[Option[Athlete]] = getJsonById(id, ATHLETE_TABLE).map(_.map(JsonIo.read[Athlete]))

  // clubs
  override def store(club: Club): Future[Unit] = upsert(JsonIo.write(club), club.id, CLUB_TABLE)

  override def getClub(id: Long): Future[Option[Club]] = getJsonById(id, CLUB_TABLE).map(_.map(JsonIo.read[Club]))

  // weather
  override def listRecentForecast(location: String, limit: Int): Future[Iterable[WeatherForecast]] = ???
  override def storeWeather(forecast: Iterable[WeatherForecast]): Future[Unit] = ???
  override def getSunriseSunset(location: String, localDate: String): Future[Option[SunriseSunset]] = ???
  override def storeSunriseSunset(sunriseSunset: SunriseSunset): Future[Unit] = ???

  // attributes
  override def storeAttribute(key: String, `type`: String, value: String): Future[Unit] = ???
  override def getAttribute(key: String, `type`: String): Future[Option[String]] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    db = Some(client.getDB(DB_NAME))
    db.getCollection(ACTIVITY_TABLE).createIndex("{id:1}", "id", true)
    db.getCollection(ACCOUNT_TABLE).createIndex("{athleteId:1}", "athleteId", true)
    db.getCollection(CLUB_TABLE).createIndex("{id:1}", "id", true)
    db.getCollection(ATHLETE_TABLE).createIndex("{id:1}", "id", true)
  }

  // releases any connections, resources used
  override def destroy() {
    client.close()
  }

  override def backup(fileName: String) = ???
}

object MongoDbStorage {

  val DB_NAME = "velocorner"
  val ACTIVITY_TABLE = "activity"
  val ACCOUNT_TABLE = "account"
  val CLUB_TABLE = "club"
  val ATHLETE_TABLE = "athlete"

  implicit def dbOrFail(db: Option[MongoDB]): MongoDB = db.getOrElse(sys.error("db is not initialized"))

  implicit def json2DbObject(json: String): DBObject = JSON.parse(json).asInstanceOf[DBObject]

  implicit def cursor2Json(cursor: DBCursor): Seq[String] = cursor.toArray.asScala.map(JSON.serialize(_))
}
