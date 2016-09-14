package velocorner.storage
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection
import org.slf4s.Logging
import velocorner.model._
import RethinkDbStorage._

import scala.language.implicitConversions

/**
  * Created by levi on 14/09/16.
  */
class RethinkDbStorage extends Storage with Logging {

  lazy val client = RethinkDB.r
  var maybeConn: Option[Connection] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) {
    val tableNames: java.util.ArrayList[String] = client.tableList().run(maybeConn)
    if (!tableNames.contains(ACTIVITY_TABLE)) client.tableCreate(ACTIVITY_TABLE).run(maybeConn)
    activities.foreach{a =>
      client.table(ACTIVITY_TABLE).insert(a).run(maybeConn)
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = ???

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
    val dbNames: java.util.ArrayList[String] = client.dbList().run(conn)
    if (!dbNames.contains(DB_NAME)) client.dbCreate(DB_NAME).run(conn)
    conn.use(DB_NAME)
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
