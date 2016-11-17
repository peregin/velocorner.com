package velocorner.storage

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.orientechnologies.orient.server.OServer
import org.slf4s.Logging
import velocorner.model._
import velocorner.storage.OrientDbStorage._
import velocorner.util.JsonIo

import collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Created by levi on 14.11.16.
  */
class OrientDbStorage extends Storage with Logging {

  @volatile var db: Option[ODatabaseDocumentTx] = None
  var server: Option[OServer] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) = inTx {
    activities.foreach { a =>
      val doc = new ODocument(ACTIVITY_CLASS)
      doc.fromJSON(JsonIo.write(a))
      doc.save()
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = inTx {
    val results: java.util.List[ODocument] = db.query(
      new OSQLSynchQuery[ODocument](
        s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId"
      )
    )
    val activities = results.asScala.map(d => JsonIo.read[Activity](d.toJSON))
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
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

    val rootDir = "orientdb_data"

    val config =
      s"""
        |<orient-server>
        |    <handlers />
        |    <network>
        |        <protocols>
        |            <protocol name="http" implementation="com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb"/>
        |        </protocols>
        |        <listeners>
        |            <listener ip-address="127.0.0.1" port-range="2480" protocol="http">
        |                <commands>
        |                    <command
        |                        pattern="GET|www GET|studio/ GET| GET|*.htm GET|*.html GET|*.xml GET|*.jpeg GET|*.jpg GET|*.png GET|*.gif GET|*.js GET|*.css GET|*.swf GET|*.ico GET|*.txt"
        |                        implementation="com.orientechnologies.orient.server.network.protocol.http.command.get.OServerCommandGetStaticContent">
        |                        <parameters>
        |                            <entry name="http.cache:*.htm *.html" value="Cache-Control: no-cache, no-store, max-age=0, must-revalidate\r\nPragma: no-cache" />
        |                            <entry name="http.cache:default" value="Cache-Control: max-age=120" />
        |                        </parameters>
        |                    </command>
        |                </commands>
        |            </listener>
        |        </listeners>
        |    </network>
        |    <storages>
        |        <storage name="velocorner" path="file:velocorner" userName="admin" userPassword="admin" loaded-at-startup="true"/>
        |    </storages>
        |    <users>
        |        <user name="admin" password="admin" resources="*"/>
        |        <user name="root" password="root" resources="*"/>
        |        <user password="guest" name="guest" resources="connect,server.listDatabases,server.dblist"/>
        |    </users>
        |    <properties>
        |        <entry name="server.database.path" value="$rootDir/server"/>
        |        <entry name="plugin.directory" value="$rootDir/plugins"/>
        |        <entry name="log.console.level" value="info"/>
        |    </properties>
        |</orient-server>
      """.stripMargin

    val oserver = new OServer
    oserver.startup(config).activate()
    server = Some(oserver)

    val odb = new ODatabaseDocumentTx(s"plocal:$rootDir/velocorner")
    if (!odb.exists()) {
      odb.create()
      odb.close()
    }
    db = Some(odb)

    inTx {
      val schema = odb.getMetadata.getSchema
      if (!schema.existsClass(ACTIVITY_CLASS)) schema.createClass(ACTIVITY_CLASS)
      val clazz = schema.getClass(ACTIVITY_CLASS)
      if (!clazz.existsProperty("id")) clazz.createProperty("id", OType.INTEGER)
      if (!clazz.areIndexed("id")) clazz.createIndex("idActivity", OClass.INDEX_TYPE.UNIQUE, "id")
    }
  }

  // releases any connections, resources used
  override def destroy() {
    server.foreach(_.shutdown())
  }

  def inTx[T](body: => T): T = {
    db.open("admin", "admin")
    import scala.util.control.Exception._
    ultimately(db.close()).apply(body)
  }
}

object OrientDbStorage {

  val ACTIVITY_CLASS = "Activity"

  implicit def dbOrFail(db: Option[ODatabaseDocumentTx]): ODatabaseDocumentTx = db.getOrElse(sys.error("db is not initialized"))
}
