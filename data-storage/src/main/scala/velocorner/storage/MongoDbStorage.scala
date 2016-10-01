package velocorner.storage

import velocorner.model._
import velocorner.util.JsonIo
import MongoDbStorage._
import com.mongodb.{DBCursor, DBObject}
import com.mongodb.casbah.{MongoClient, MongoDB}
import com.mongodb.util.JSON
import com.mongodb.casbah.query.Imports._
import org.slf4s.Logging

import scala.language.implicitConversions
import collection.JavaConverters._

/**
  * Created by levi on 28/09/16.
  * Access layer to the MongoDb.
  */
class MongoDbStorage extends Storage with Logging {

  lazy val client = MongoClient()
  var db: Option[MongoDB] = None


  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) {
    val coll = db.getCollection(ACTIVITY_TABLE)
    // TODO: bulk store
    activities.foreach{ a =>
      val json = JsonIo.write(a)
      val upd = "id" $eq a.id
      coll.update(upd, json, true, false)
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = $and("athlete.id" $eq athleteId, "type" $eq "Ride")
    val results = coll.find(query)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
  }

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = "type" $eq "Ride"
    val results = coll.find(query).sort("{start_date:-1}").limit(limit)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found activities ${activities.size}")
    AthleteDailyProgress.fromStorage(activities).toList.sortBy(_.dailyProgress.day.toString)
  }

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = "type" $eq "Ride"
    val results = coll.find(query).sort("{start_date:-1}").limit(limit)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found recent activities ${activities.size}")
    activities
  }


  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val query = $and("athlete.id" $eq athleteId, "type" $eq "Ride")
    val results = coll.find(query).sort("{start_date:-1}").limit(limit)
    val activities = results.map(JsonIo.read[Activity])
    log.debug(s"found recent activities ${activities.size} for $athleteId")
    activities
  }

  // accounts
  override def store(account: Account): Unit = ???

  override def getAccount(id: Long): Option[Account] = ???

  // athletes
  override def store(athlete: Athlete): Unit = ???

  override def getAthlete(id: Long): Option[Athlete] = ???

  // clubs
  override def store(club: Club): Unit = ???

  override def getClub(id: Long): Option[Club] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    db = Some(client.getDB(DB_NAME))
    db.getCollection(ACTIVITY_TABLE).createIndex("{id:1}", "id", true)
    db.getCollection(ACCOUNT_TABLE).createIndex("{id:1}", "id", true)
    db.getCollection(CLUB_TABLE).createIndex("{id:1}", "id", true)
    db.getCollection(ATHLETE_TABLE).createIndex("{id:1}", "id", true)
  }

  // releases any connections, resources used
  override def destroy() {
    client.close()
  }
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
