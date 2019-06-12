package velocorner.storage

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1}
import com.rethinkdb.net.{Connection, Cursor}
import org.json.simple.JSONObject
import org.slf4s.Logging
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.{SunriseSunset, WeatherForecast}
import velocorner.storage.RethinkDbStorage._
import velocorner.util.JsonIo

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.language.implicitConversions

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by levi on 14/09/16.
  *
  * From Data Explorer:
  * <code>
  *   r.db('velocorner').table('activity');
  * </code>
  */
class RethinkDbStorage extends Storage with Logging {

  lazy val client = RethinkDB.r
  @volatile var maybeConn: Option[Connection] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = Future {
    activities.map{ a =>
      val json = JsonIo.write(a)
      client.json(json)
    }.foreach{json =>
      // TODO: bulk store
      val result: java.util.HashMap[String, String] = client.table(ACTIVITY_TABLE).insert(json).optArg("conflict", "update").run(maybeConn)
      log.debug(s"result $result")
    }
  }


  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]] = Future {
    val result: Cursor[java.util.HashMap[String, String]] = client.table(ACTIVITY_TABLE).filter(reqlFunction1{ arg1 =>
      val field1 = arg1.getField("athlete").getField("id")
      val field2 = arg1.getField("type")
      field1.eq(athleteId).and(field2.eq("Ride"))
    }).run(maybeConn)
    val activities = result2Activity(result.toList.asScala.toList)
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
  }

  override def getActivity(id: Long): Future[Option[Activity]] = getJsonById(id, ACTIVITY_TABLE).map(_.map(JsonIo.read[Activity]))

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = Future {
    val result: java.util.ArrayList[java.util.HashMap[String, String]] = client.table(ACTIVITY_TABLE).filter(reqlFunction1{ arg1 =>
      val field1 = arg1.getField("athlete").getField("id")
      val field2 = arg1.getField("type")
      field1.eq(athleteId).and(field2.eq("Ride"))
    }).orderBy(client.desc("start_date")).limit(limit).run(maybeConn)
    val activities = result2Activity(result.asScala.toList)
    log.debug(s"found recent activities ${activities.size} for $athleteId")
    activities
  }

  private def result2Activity(result: List[java.util.HashMap[String, String]]): Iterable[Activity] = {
    val mapList = result//result.toList.asScala.toList
    mapList.map(JSONObject.toJSONString).map(JsonIo.read[Activity] _)
  }

  private def upsert[T](jsText: T, table: String): Future[Unit] = Future {
    val json = client.json(jsText)
    val result: java.util.HashMap[String, String] = client.table(table).insert(json).optArg("conflict", "update").run(maybeConn)
    log.debug(s"result $result")
  }

  private def getJsonById(id: Long, table: String): Future[Option[String]] = Future {
    val result: Cursor[java.util.HashMap[String, String]] = client.table(table).filter(reqlFunction1{ arg1 =>
      val field1 = arg1.getField("id")
      field1.eq(id)
    }).run(maybeConn)
    result.toList.asScala.toList.map(JSONObject.toJSONString).headOption
  }

  // accounts
  override def store(account: Account): Future[Unit] = upsert(JsonIo.write(account), ACCOUNT_TABLE)

  override def getAccount(id: Long): Future[Option[Account]] = getJsonById(id, ACCOUNT_TABLE).map(_.map(JsonIo.read[Account]))

  // athletes
  override def store(athlete: Athlete): Future[Unit] = upsert(JsonIo.write(athlete), ATHLETE_TABLE)

  override def getAthlete(id: Long): Future[Option[Athlete]] = getJsonById(id, ATHLETE_TABLE).map(_.map(JsonIo.read[Athlete]))

  // clubs
  override def store(club: Club): Future[Unit] = upsert(JsonIo.write(club), CLUB_TABLE)

  override def getClub(id: Long): Future[Option[Club]] = getJsonById(id, CLUB_TABLE).map(_.map(JsonIo.read[Club]))

  // weather
  override def getWeatherStorage(): WeatherStorage = ???

  // attributes
  override def getAttributeStorage(): AttributeStorage = ???

  // various achievements
  override def getAchievementStorage(): AchievementStorage = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    val conn = client.connection().hostname("localhost").port(28015).connect()

    // create database if not present
    val dbNames: java.util.ArrayList[String] = client.dbList().run(conn)
    if (!dbNames.contains(DB_NAME)) client.dbCreate(DB_NAME).run(conn)

    conn.use(DB_NAME)

    // create tables if not present
    def createIfNotExists(tables: String*) {
      val tableNames: java.util.ArrayList[String] = client.tableList().run(conn)
      tables.foreach{ t =>
        if (!tableNames.contains(t)) client.tableCreate(t).run(conn)
      }
    }
    createIfNotExists(ACTIVITY_TABLE, ACCOUNT_TABLE, ATHLETE_TABLE, CLUB_TABLE)

    maybeConn = Some(conn)
    log.info(s"connected with $conn")
  }

  // releases any connections, resources used
  override def destroy() {
    maybeConn.close
  }

  override def backup(fileName: String) = ???
}

object RethinkDbStorage {

  val DB_NAME = "velocorner"
  val ACTIVITY_TABLE = "activity"
  val ACCOUNT_TABLE = "account"
  val CLUB_TABLE = "club"
  val ATHLETE_TABLE = "athlete"

  implicit def convert(conn: Option[Connection]): Connection = conn.getOrElse(sys.error("connection is not initialized"))

  implicit def reqlFunction1(fun: (ReqlExpr) => Object): ReqlFunction1 = new ReqlFunction1 {
    override def apply(arg1: ReqlExpr): Object = fun(arg1)
  }
}
