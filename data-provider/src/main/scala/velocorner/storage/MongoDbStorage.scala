package velocorner.storage

import com.mongodb.client.model.{IndexModel, UpdateOptions}
import com.typesafe.scalalogging.LazyLogging
import org.mongodb.scala._
import org.bson.BsonDocument
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.IndexOptions
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.storage.MongoDbStorage._
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

import scalaz.std.list._
import scalaz.std.scalaFuture._
import scalaz.syntax.traverse.ToTraverseOps

/**
  * Created by levi on 28/09/16.
  * Access layer to the MongoDb.
  * TODO: use bulk upsert
  * TODO: use async API
  */
class MongoDbStorage extends Storage with LazyLogging {

  lazy val client = MongoClient()
  @volatile var db: Option[MongoDatabase] = None


  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    activities.toList.traverseU { a =>
      val json = JsonIo.write(a)
      coll
        .updateOne(equal("id", a.id), json, new UpdateOptions()
        .upsert(true))
        .toFuture()
    }.map(_ => ())
  }

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val results = coll
      .find(and(equal("athlete.id", athleteId), equal("type", "Ride")))
      .toFuture()
    for {
      docs <- results
    } yield DailyProgress.fromStorage(docs.map(_.toJson()).map(JsonIo.read[Activity]))
  }

  override def getActivity(id: Long): Future[Option[Activity]] = getJsonById(id, ACTIVITY_TABLE, "id").map(_.map(JsonIo.read[Activity]))

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val results = coll
      .find(and(equal("athlete.id", athleteId), equal("type", "Ride")))
      .sort("{start_date:-1}")
      .limit(limit)
      .toFuture()
    for {
      docs <- results
    } yield docs.map(_.toJson()).map(JsonIo.read[Activity]) 
  }

  private def upsert(json: String, id: Long, collName: String, idName: String = "id"): Future[Unit] = {
    val coll = db.getCollection(collName)
    coll
      .updateOne(equal(idName, id), json, new UpdateOptions()
        .upsert(true)).toFuture().map(_ => ())
  }

  private def getJsonById(id: Long, collName: String, idName: String = "id"): Future[Option[String]] = {
    val coll = db.getCollection(collName)
    for {
      doc <- coll.find(equal(idName, id)).headOption
    } yield doc.map(_.toJson())
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
  override def getWeatherStorage(): WeatherStorage = ???

  // attributes
  override def getAttributeStorage(): AttributeStorage = ???

  // various achievements
  override def getAchievementStorage(): AchievementStorage = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize(): Unit = {
    db = Some(client.getDatabase(DB_NAME))
    db.getCollection(ACTIVITY_TABLE).createIndexes(Seq(new IndexModel("{id:1}", new IndexOptions().name("id").unique(true))))
    db.getCollection(ACCOUNT_TABLE).createIndexes(Seq(new IndexModel("{athleteId:1}", new IndexOptions().name("athleteId").unique(true))))
    db.getCollection(CLUB_TABLE).createIndexes(Seq(new IndexModel("{id:1}", new IndexOptions().name("id").unique(true))))
    db.getCollection(ATHLETE_TABLE).createIndexes(Seq(new IndexModel("{id:1}", new IndexOptions().name("id").unique(true))))
  }

  // releases any connections, resources used
  override def destroy(): Unit = {
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

  implicit def dbOrFail(db: Option[MongoDatabase]): MongoDatabase = db.getOrElse(sys.error("db is not initialized"))

  implicit def json2Bson(json: String): Bson = BsonDocument.parse(json)
}
