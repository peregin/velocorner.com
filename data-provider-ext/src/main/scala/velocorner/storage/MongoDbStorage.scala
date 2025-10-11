package velocorner.storage

import cats.implicits._
import com.mongodb.client.model.{IndexModel, UpdateOptions}
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonDocument
import org.joda.time.DateTime
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.IndexOptions
import velocorner.api.Account
import velocorner.api.strava.Activity
import velocorner.model._
import velocorner.model.strava.Gear
import velocorner.storage.MongoDbStorage._
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

/**
 * Created by levi on 28/09/16.
 * Access layer to the MongoDb.
 */
//noinspection NotImplementedCode
class MongoDbStorage extends Storage[Future] with LazyLogging {

  lazy private val client = MongoClient()
  @volatile var db: Option[MongoDatabase] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    activities.toList.traverse { a =>
      val json = JsonIo.write(a)
      coll
        .updateOne(
          equal("id", a.id),
          json,
          new UpdateOptions()
            .upsert(true)
        )
        .toFuture()
    }.void
  }

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def listActivityYears(athleteId: Long, activityType: String): Future[Iterable[Int]] = ???

  override def listAllActivities(athleteId: Long, activityType: String): Future[Iterable[Activity]] = {
    val coll = db.getCollection(ACTIVITY_TABLE)
    val results = coll
      .find(and(equal("athlete.id", athleteId), equal("type", activityType)))
      .toFuture()
    for {
      docs <- results
    } yield docs.map(_.toJson()).map(JsonIo.read[Activity])
  }

  override def listYtdActivities(athleteId: Long, activityType: String, year: Int): Future[Iterable[Activity]] = ???

  override def listActivities(athleteId: Long, from: DateTime, to: DateTime): Future[Iterable[Activity]] = ???

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

  override def listTopActivities(athleteId: Long, actionType: ActionType.Entry, activityType: String, limit: Int): Future[Iterable[Activity]] = ???

  override def getLastActivity(athleteId: Long): Future[Option[Activity]] = ???

  override def getActivity(id: Long): Future[Option[Activity]] = getJsonById(id.toString, ACTIVITY_TABLE).map(_.map(JsonIo.read[Activity]))

  override def suggestActivities(snippet: String, athleteId: Long, max: Int): Future[Iterable[Activity]] = Future(Iterable.empty)

  override def activityTitles(athleteId: Long, max: Int): Future[Iterable[String]] = Future(Iterable.empty)

  private def upsert(json: String, id: String, collName: String, idName: String = "id"): Future[Unit] = {
    val coll = db.getCollection(collName)
    coll
      .updateOne(
        equal(idName, id),
        json,
        new UpdateOptions()
          .upsert(true)
      )
      .toFuture()
      .void
  }

  private def getJsonById(id: String, collName: String, idName: String = "id"): Future[Option[String]] = {
    val coll = db.getCollection(collName)
    for {
      doc <- coll.find(equal(idName, id)).headOption()
    } yield doc.map(_.toJson())
  }

  // accounts
  override def getAccountStorage: AccountStorage[Future] = accountStorage
  private lazy val accountStorage = new AccountStorage[Future] {
    override def store(account: Account): Future[Unit] =
      upsert(JsonIo.write(account), account.athleteId.toString, ACCOUNT_TABLE, "athleteId")
    override def getAccount(id: Long): Future[Option[Account]] =
      getJsonById(id.toString, ACCOUNT_TABLE, "athleteId").map(_.map(JsonIo.read[Account]))
  }

  // gears
  override def getGearStorage: GearStorage[Future] = gearStorage
  private lazy val gearStorage = new GearStorage[Future] {
    override def store(gear: Gear, `type`: Gear.Entry, athleteId: Long): Future[Unit] = upsert(JsonIo.write(gear), gear.id, GEAR_TABLE)
    override def getGear(id: String): Future[Option[Gear]] = getJsonById(id, GEAR_TABLE).map(_.map(JsonIo.read[Gear]))
    override def listGears(athleteId: Long): Future[Iterable[Gear]] = Future(Iterable.empty)
  }

  // various achievements
  override def getAchievementStorage: AchievementStorage[Future] = ???

  override def getAdminStorage: AdminStorage[Future] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize(): Unit = {
    db = Some(client.getDatabase(DB_NAME))
    db.getCollection(ACTIVITY_TABLE).createIndexes(Seq(new IndexModel("{id:1}", new IndexOptions().name("id").unique(true))))
    db.getCollection(ACCOUNT_TABLE).createIndexes(Seq(new IndexModel("{athleteId:1}", new IndexOptions().name("athleteId").unique(true))))
    db.getCollection(GEAR_TABLE).createIndexes(Seq(new IndexModel("{id:1}", new IndexOptions().name("id").unique(true))))
  }

  // releases any connections, resources used
  override def destroy(): Unit =
    client.close()
}

object MongoDbStorage {

  val DB_NAME = "velocorner"
  val ACTIVITY_TABLE = "activity"
  val ACCOUNT_TABLE = "account"
  val GEAR_TABLE = "gear"

  implicit def dbOrFail(db: Option[MongoDatabase]): MongoDatabase = db.getOrElse(sys.error("db is not initialized"))

  implicit def json2Bson(json: String): Bson = BsonDocument.parse(json)
}
