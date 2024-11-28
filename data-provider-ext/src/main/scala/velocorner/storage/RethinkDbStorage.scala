package velocorner.storage

import cats._
import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1}
import com.rethinkdb.net.{Connection, Result}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.json.simple.JSONObject
import velocorner.api.strava.Activity
import velocorner.model.strava.Gear
import velocorner.model.{Account, ActionType}
import velocorner.storage.RethinkDbStorage._
import velocorner.util.JsonIo

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

// for kestrel combinator and unsafeTap
import mouse.all._

/**
 * Created by levi on 14/09/16.
 *
 * From Data Explorer:
 * <code>
 *   r.db('velocorner').table('activity');
 * </code>
 */
//noinspection NotImplementedCode
class RethinkDbStorage[M[_]: Monad] extends Storage[M] with LazyLogging {

  private lazy val client = RethinkDB.r
  @volatile private var maybeConn: Option[Connection] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): M[Unit] = Monad[M].pure(
    activities
      .map { a =>
        val json = JsonIo.write(a)
        client.json(json)
      }
      .foreach { json =>
        val result: Result[Object] = client.table(ACTIVITY_TABLE).insert(json).optArg("conflict", "update").run(maybeConn)
        logger.debug(s"result $result")
      }
  )

  override def listActivityTypes(athleteId: Long): M[Iterable[String]] = Monad[M].pure(Iterable.empty[String])

  override def listActivityYears(athleteId: Long, activityType: String): M[Iterable[Int]] = Monad[M].pure(Iterable.empty[Int])

  // Cursor[java.util.HashMap[String, String]]
  override def listAllActivities(athleteId: Long, activityType: String): M[Iterable[Activity]] = Monad[M].pure {
    val result: Result[java.util.HashMap[String, String]] = client
      .table(ACTIVITY_TABLE)
      .filter(reqlFunction1 { arg1 =>
        val field1 = arg1.getField("athlete").getField("id")
        val field2 = arg1.getField("type")
        field1.eq(athleteId, Nil).and(field2.eq(activityType, Nil))
      })
      .run(maybeConn)
      .asInstanceOf[Result[java.util.HashMap[String, String]]]
    result2Activity(result.toList.asScala.toList)
  }

  override def listYtdActivities(athleteId: Long, activityType: String, year: Int): M[Iterable[Activity]] = ???

  override def listActivities(athleteId: Long, from: DateTime, to: DateTime): M[Iterable[Activity]] = ???

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): M[Iterable[Activity]] = Monad[M].pure {
    val result: Result[java.util.HashMap[String, String]] = client
      .table(ACTIVITY_TABLE)
      .filter(reqlFunction1 { arg1 =>
        val field1 = arg1.getField("athlete").getField("id")
        val field2 = arg1.getField("type")
        field1.eq(athleteId, Nil).and(field2.eq("Ride", Nil))
      })
      .orderBy(client.desc("start_date"))
      .limit(limit)
      .run(maybeConn)
      .asInstanceOf[Result[java.util.HashMap[String, String]]]
    result2Activity(result.toList.asScala.toList) <| (a => logger.debug(s"found recent activities ${a.size} for $athleteId"))
  }

  override def listTopActivities(athleteId: Long, actionType: ActionType.Entry, activityType: String, limit: Int): M[Iterable[Activity]] =
    Monad[M].pure(Iterable.empty)

  override def getLastActivity(athleteId: Long): M[Option[Activity]] = Monad[M].pure(None)

  override def getActivity(id: Long): M[Option[Activity]] =
    Monad[M].map(getJsonById(id.toString, ACTIVITY_TABLE))(_.map(JsonIo.read[Activity]))

  override def suggestActivities(snippet: String, athleteId: Long, max: Int): M[Iterable[Activity]] = Monad[M].pure(Iterable.empty)

  override def activityTitles(athleteId: Long, max: Int): M[Iterable[String]] = Monad[M].pure(Iterable.empty)

  private def result2Activity(result: List[java.util.HashMap[String, String]]): Iterable[Activity] =
    result.map(JSONObject.toJSONString).map(JsonIo.read[Activity])

  private def upsert[T](jsText: T, table: String): M[Unit] = Monad[M].pure {
    val json = client.json(jsText)
    val result: Result[AnyRef] = client.table(table).insert(json).optArg("conflict", "update").run(maybeConn)
    logger.debug(s"result $result")
  }

  private def getJsonById(id: String, table: String): M[Option[String]] = Monad[M].pure {
    val result: Result[java.util.HashMap[String, String]] = client
      .table(table)
      .filter(reqlFunction1 { arg1 =>
        val field1 = arg1.getField("id")
        field1.eq(id)
      })
      .run(maybeConn)
      .asInstanceOf[Result[java.util.HashMap[String, String]]]
    result.toList.asScala.toList.map(JSONObject.toJSONString).headOption
  }

  // accounts
  override def getAccountStorage: AccountStorage[M] = accountStorage
  private lazy val accountStorage = new AccountStorage[M] {
    override def store(account: Account): M[Unit] = upsert(JsonIo.write(account), ACCOUNT_TABLE)
    override def getAccount(id: Long): M[Option[Account]] =
      Monad[M].map(getJsonById(id.toString, ACCOUNT_TABLE))(_.map(JsonIo.read[Account]))
  }

  // gears
  override def getGearStorage: GearStorage[M] = gearStorage
  private lazy val gearStorage = new GearStorage[M] {
    override def store(gear: Gear, `type`: Gear.Entry, athleteId: Long): M[Unit] = upsert(JsonIo.write(gear), GEAR_TABLE)
    override def getGear(id: String): M[Option[Gear]] = Monad[M].map(getJsonById(id, GEAR_TABLE))(_.map(JsonIo.read[Gear]))
    override def listGears(athleteId: Long): M[Iterable[Gear]] = ???
  }

  // various achievements
  override def getAchievementStorage: AchievementStorage[M] = ???

  override def getAdminStorage: AdminStorage[M] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize(): Unit = {
    val conn = client.connection().hostname("localhost").port(28015).connect()

    // create database if not present
    val dbNames: Result[java.util.ArrayList[String]] = client.dbList().run(conn).asInstanceOf[Result[java.util.ArrayList[String]]]
    if (!dbNames.toList.contains(DB_NAME)) client.dbCreate(DB_NAME).run(conn)

    conn.use(DB_NAME)

    // create tables if not present
    def createIfNotExists(tables: String*): Unit = {
      val tableNames: Result[java.util.ArrayList[String]] = client.tableList().run(conn).asInstanceOf[Result[java.util.ArrayList[String]]]
      tables.foreach { t =>
        if (!tableNames.toList.contains(t)) client.tableCreate(t).run(conn)
      }
    }
    createIfNotExists(ACTIVITY_TABLE, ACCOUNT_TABLE, GEAR_TABLE)

    maybeConn = Option(conn)
    logger.info(s"connected with $conn")
  }

  // releases any connections, resources used
  override def destroy(): Unit = maybeConn.close()
}

object RethinkDbStorage {

  val DB_NAME = "velocorner"
  val ACTIVITY_TABLE = "activity"
  val ACCOUNT_TABLE = "account"
  val GEAR_TABLE = "gear"

  implicit def convert(conn: Option[Connection]): Connection = conn.getOrElse(sys.error("connection is not initialized"))

  implicit def reqlFunction1(fun: ReqlExpr => Object): ReqlFunction1 = (arg1: ReqlExpr) => fun(arg1)
}
