package velocorner.storage

import java.io.FileOutputStream

import com.orientechnologies.orient.core.command.OCommandOutputListener
import com.orientechnologies.orient.core.db.document.{ODatabaseDocument, ODatabaseDocumentTx}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.orientechnologies.orient.server.OServer
import org.slf4s.Logging
import play.api.libs.json.{Reads, Writes}
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.WeatherForecast
import velocorner.storage.OrientDbStorage._
import velocorner.util.{CloseableResource, JsonIo, Metrics}

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scala.util.control.Exception._

/**
 * Created by levi on 14.11.16.
 */
class OrientDbStorage(val rootDir: String, storageType: StorageType = LocalStorage, serverPort: Int = 2480)
  extends Storage with CloseableResource with Metrics with Logging {

  var server: Option[OServer] = None


  private def listFor[T](sql: String)(implicit fjs: Reads[T]): Seq[T] = inTx() { db =>
    val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
    results.asScala.map(d => JsonIo.read[T](d.toJSON))
  }

  private def upsert[T](payload: T, className: String, sql: String)(implicit fjs: Writes[T]): ODocument = inTx() { db =>
    val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
    val doc = results.asScala.headOption.getOrElse(new ODocument(className))
    doc.fromJSON(JsonIo.write(payload))
    doc.save()
  }

  private def lookup[T](className: String, propertyName: String, propertyValue: Int)(implicit fjs: Reads[T]): Option[T] = {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    listFor[T](sql).headOption
  }

  // FIXME: workaround until elastic is in place
  def suggestActivities(snippet: String, athleteId: Int, max: Int): Iterable[Activity] = {
    listFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' AND athlete.id = $athleteId AND name.toLowerCase() like '%${snippet.toLowerCase}%' ORDER BY start_date DESC LIMIT $max")
  }

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]) = {
    activities.foreach( a =>
      upsert(a, ACTIVITY_CLASS, s"SELECT FROM $ACTIVITY_CLASS WHERE id = ${a.id}")
    )
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = {
    val activities = listFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride'")
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
  }

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = {
    val activities = listFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
    log.debug(s"found activities ${activities.size}")
    AthleteDailyProgress.fromStorage(activities).toList.sortBy(_.dailyProgress.day.toString).reverse
  }

  override def getActivity(id: Int): Option[Activity] = lookup[Activity](ACTIVITY_CLASS, "id", id)

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = {
    listFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
  }

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = {
    listFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
  }

  // accounts
  override def store(account: Account) {
    upsert(account, ACCOUNT_CLASS, s"SELECT FROM $ACCOUNT_CLASS WHERE athleteId = ${account.athleteId}")
  }

  override def getAccount(id: Long): Option[Account] = lookup[Account](ACCOUNT_CLASS, "athleteId", id.toInt)

  // athletes
  override def store(athlete: Athlete) {
    upsert(athlete, ATHLETE_CLASS, s"SELECT FROM $ATHLETE_CLASS WHERE id = ${athlete.id}")
  }

  override def getAthlete(id: Long): Option[Athlete] = lookup[Athlete](ATHLETE_CLASS, "id", id.toInt)

  // clubs
  override def store(club: Club) {
    upsert(club, CLUB_CLASS, s"SELECT FROM $CLUB_CLASS WHERE id = ${club.id}")
  }

  override def getClub(id: Long): Option[Club] = lookup[Club](CLUB_CLASS, "id", id.toInt)

  // weather
  override def listRecentForecast(location: String, limit: Int): Iterable[WeatherForecast] = {
    listFor[WeatherForecast](s"SELECT FROM $WEATHER_CLASS WHERE location like '$location' ORDER BY timestamp DESC LIMIT $limit")
  }

  override def storeWeather(forecast: Iterable[WeatherForecast]) {
    forecast.foreach(a =>
      upsert(a, WEATHER_CLASS, s"SELECT FROM $WEATHER_CLASS WHERE location like '${a.location}' AND timestamp = ${a.timestamp}")
    )
  }

  // attributes
  override def storeAttribute(key: String, `type`: String, value: String) {
    val attr = KeyValue(key, `type`, value)
    upsert(attr, ATTRIBUTE_CLASS, s"SELECT FROM $ATTRIBUTE_CLASS WHERE type = '${`type`}' and key = '$key'")
  }

  override def getAttribute(key: String, `type`: String): Option[String] = {
    listFor[KeyValue](s"SELECT FROM $ATTRIBUTE_CLASS WHERE type = '${`type`}' AND key = '$key'")
      .headOption
      .map(_.value)
  }

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    val config =
      s"""
         |<orient-server>
         |    <handlers />
         |    <network>
         |        <protocols>
         |            <protocol name="http" implementation="com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb"/>
         |            <protocol name="binary" implementation="com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary"/>
         |        </protocols>
         |        <listeners>
         |            <listener ip-address="127.0.0.1" port-range="$serverPort" protocol="http">
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
         |            <listener ip-address="0.0.0.0" port-range="2424-2430" protocol="binary"/>
         |        </listeners>
         |    </network>
         |    <storages>
         |        <storage name="velocorner" path="${storageType.name}:$rootDir/$DATABASE_NAME" userName="admin" userPassword="admin" loaded-at-startup="true"/>
         |    </storages>
         |    <users>
         |        <user name="admin" password="admin" resources="*"/>
         |        <user name="root" password="root" resources="*"/>
         |    </users>
         |    <properties>
         |        <entry name="server.database.path" value="$rootDir/server"/>
         |        <entry name="plugin.directory" value="$rootDir/plugins"/>
         |        <entry name="log.console.level" value="info"/>
         |        <entry name="plugin.dynamic" value="false"/>
         |        <entry name="server.cache.staticResources" value="false"/>
         |    </properties>
         |</orient-server>
      """.stripMargin

    val oserver = new OServer
    oserver.startup(config).activate()
    server = Some(oserver)

    inTx() { odb =>
      if (!odb.exists()) {
        odb.create()
        odb.close()
      }

      case class IndexSetup(indexField: String, indexType: OType)

      def createIfNeeded(className: String, index: IndexSetup*) {
        val schema = odb.getMetadata.getSchema
        if (!schema.existsClass(className)) schema.createClass(className)
        val clazz = schema.getClass(className)

        index.foreach(ix =>
          if (!clazz.existsProperty(ix.indexField)) clazz.createProperty(ix.indexField, ix.indexType)
        )

        val ixFields = index.map(_.indexField).sorted
        val ixName = ixFields.mkString("-").replace(".", "_")

        if (!clazz.areIndexed(ixFields:_*)) clazz.createIndex(s"$ixName-$className", OClass.INDEX_TYPE.UNIQUE, ixFields:_*)
      }

      createIfNeeded(ACTIVITY_CLASS, IndexSetup("id", OType.INTEGER))
      createIfNeeded(ACCOUNT_CLASS, IndexSetup("athleteId", OType.INTEGER))
      createIfNeeded(CLUB_CLASS, IndexSetup("id", OType.INTEGER))
      createIfNeeded(ATHLETE_CLASS, IndexSetup("id", OType.INTEGER))
      createIfNeeded(WEATHER_CLASS, IndexSetup("location", OType.STRING), IndexSetup("timestamp", OType.LONG))
      createIfNeeded(ATTRIBUTE_CLASS, IndexSetup("key", OType.STRING))
    }
  }

  // releases any connections, resources used
  override def destroy() {
    server.foreach(_.shutdown())
    server = None
    log.info("database has been closed...")
  }

  override def backup(fileName: String) = timed("backup") {
    val listener = new OCommandOutputListener {
      override def onMessage(iText: String)= log.debug(s"backup $iText")
    }
    withCloseable(new FileOutputStream(fileName)) { fos =>
      server.foreach { s =>
        val internal = s.openDatabase("velocorner")
        internal.backup(fos, null, null, listener, 1, 4096)
      }
    }
  }


  def inTx[T](storageType: StorageType = RemoteStorage)(body:ODatabaseDocument => T): T = {
    val url = storageType match {
      case RemoteStorage => s"${storageType.name}:localhost/$rootDir/$DATABASE_NAME"
      case _ => s"${storageType.name}:$rootDir/$DATABASE_NAME"
    }
    val dbDoc = new ODatabaseDocumentTx(url)
    if (!dbDoc.isActiveOnCurrentThread) dbDoc.activateOnCurrentThread()
    dbDoc.open(ADMIN_USER, ADMIN_PASSWORD)
    ultimately(dbDoc.close()).apply {
      body(dbDoc)
    }
  }
}

sealed abstract class StorageType(val name: String)
case object RemoteStorage extends StorageType("remote")
case object LocalStorage extends StorageType("plocal")
case object MemoryStorage extends StorageType("memory")

object OrientDbStorage {

  val ADMIN_USER = "admin"
  val ADMIN_PASSWORD = "admin"

  val DATABASE_NAME = "velocorner"

  val ACTIVITY_CLASS = "Activity"
  val ACCOUNT_CLASS = "Account"
  val CLUB_CLASS = "Club"
  val ATHLETE_CLASS = "Athlete"
  val WEATHER_CLASS = "Weather"
  val ATTRIBUTE_CLASS = "Attribute"

}
