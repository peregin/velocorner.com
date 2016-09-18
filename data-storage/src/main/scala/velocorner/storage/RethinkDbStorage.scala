package velocorner.storage
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.{Connection, Cursor}
import org.slf4s.Logging
import velocorner.model._
import RethinkDbStorage._
import velocorner.util.JsonIo

import scala.language.implicitConversions

/**
  * Created by levi on 14/09/16.
  */
class RethinkDbStorage extends Storage with Logging {

  lazy val client = RethinkDB.r
  var maybeConn: Option[Connection] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) {
    val dbArgs = activities.map{ a =>
      val json = JsonIo.write(a)
      client.json(json)
    }
    val result: java.util.HashMap[_, _] = client.table(ACTIVITY_TABLE).insert(dbArgs).run(maybeConn)
    log.info(s"result $result")
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = {
    val result: Cursor[_] = client.table(ACTIVITY_TABLE).run(maybeConn)
    log.info(s"result $result")
    Seq.empty
  }

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = ???

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = ???

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = ???

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
    val conn = client.connection().hostname("localhost").port(28015).connect()

    // create database if not present
    val dbNames: java.util.ArrayList[String] = client.dbList().run(conn)
    if (!dbNames.contains(DB_NAME)) client.dbCreate(DB_NAME).run(conn)

    conn.use(DB_NAME)

    // create tables if not present
    val tableNames: java.util.ArrayList[String] = client.tableList().run(conn)
    if (!tableNames.contains(ACTIVITY_TABLE)) client.tableCreate(ACTIVITY_TABLE).run(conn)

    maybeConn = Some(conn)
    log.info(s"connected with $conn")
  }

  // releases any connections, resources used
  override def destroy() {
    maybeConn.close
  }
}

object RethinkDbStorage {

  val DB_NAME = "velocorner"
  val ACTIVITY_TABLE = "activity"

  implicit def convert(conn: Option[Connection]): Connection = conn.getOrElse(sys.error("connection is not initialized"))
}
