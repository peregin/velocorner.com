package velocorner.storage

import java.io.FileOutputStream

import com.orientechnologies.orient.core.command.{OCommandOutputListener, OCommandResultListener}
import com.orientechnologies.orient.core.db.document.{ODatabaseDocument, ODatabaseDocumentTx}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.{OSQLNonBlockingQuery}
import com.orientechnologies.orient.server.OServer
import org.slf4s.Logging
import play.api.libs.json.{Format, Json, Reads, Writes}
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.{SunriseSunset, WeatherForecast}
import velocorner.storage.OrientDbStorage._
import velocorner.util.{CloseableResource, JsonIo, Metrics}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.Try
import scala.util.control.Exception._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._
import scalaz.syntax.traverse.ToTraverseOps

import scala.collection.JavaConverters._

/**
 * Created by levi on 14.11.16.
 */
class OrientDbStorage(val rootDir: String, storageType: StorageType = LocalStorage, serverPort: Int = 2480)
  extends Storage with CloseableResource with Metrics with Logging {

  var server: Option[OServer] = None

  private def lookup[T](className: String, propertyName: String, propertyValue: Long)(implicit fjs: Reads[T]): Future[Option[T]] = {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    queryForOption[T](sql)
  }

  // FIXME: workaround until elastic is in place
  def suggestActivities(snippet: String, athleteId: Long, max: Int): Future[Iterable[Activity]] = {
    queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' AND athlete.id = $athleteId AND name.toLowerCase() like '%${snippet.toLowerCase}%' ORDER BY start_date DESC LIMIT $max")
  }

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = {
    activities
      .toList
      .traverseU(a => upsert(a, ACTIVITY_CLASS, s"SELECT FROM $ACTIVITY_CLASS WHERE id = ${a.id}"))
      .map(_ => ())
  }

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = Future { inTx() { db =>
    val results = db.query(s"SELECT type AS name, COUNT(*) AS counter FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId GROUP BY name ORDER BY counter DESC")
    results.asScala.map(d => JsonIo.read[Counter](d.toJSON)).map(_.name).toIterable
  }}

  override def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]] = for {
    activities <- queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride'")
    _ = log.debug(s"found activities ${activities.size} for $athleteId")
  } yield DailyProgress.fromStorage(activities)

  override def getActivity(id: Long): Future[Option[Activity]] = lookup[Activity](ACTIVITY_CLASS, "id", id)

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = {
    queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
  }

  // accounts
  override def store(account: Account): Future[Unit] = {
    upsert(account, ACCOUNT_CLASS, s"SELECT FROM $ACCOUNT_CLASS WHERE athleteId = ${account.athleteId}")
  }

  override def getAccount(id: Long): Future[Option[Account]] = lookup[Account](ACCOUNT_CLASS, "athleteId", id)

  // athletes
  override def store(athlete: Athlete): Future[Unit] = {
    upsert(athlete, ATHLETE_CLASS, s"SELECT FROM $ATHLETE_CLASS WHERE id = ${athlete.id}")
  }

  override def getAthlete(id: Long): Future[Option[Athlete]] = lookup[Athlete](ATHLETE_CLASS, "id", id)

  // clubs
  override def store(club: Club): Future[Unit] = {
    upsert(club, CLUB_CLASS, s"SELECT FROM $CLUB_CLASS WHERE id = ${club.id}")
  }

  override def getClub(id: Long): Future[Option[Club]] = lookup[Club](CLUB_CLASS, "id", id)


  lazy val weatherStorage = new WeatherStorage {
    override def listRecentForecast(location: String, limit: Int): Future[Iterable[WeatherForecast]] = {
      queryFor[WeatherForecast](s"SELECT FROM $WEATHER_CLASS WHERE location like '$location' ORDER BY timestamp DESC LIMIT $limit")
    }
    override def storeWeather(forecast: Iterable[WeatherForecast]): Future[Unit] = {
      forecast.toList.traverseU(a => upsert(a, WEATHER_CLASS, s"SELECT FROM $WEATHER_CLASS WHERE location like '${a.location}' AND timestamp = ${a.timestamp}"))
        .map(_ => ())
    }
    override def getSunriseSunset(location: String, localDate: String): Future[Option[SunriseSunset]] =
      queryForOption[SunriseSunset](s"SELECT FROM $SUN_CLASS WHERE location like '$location' AND date = '$localDate'")
    override def storeSunriseSunset(sunriseSunset: SunriseSunset): Future[Unit] = {
      upsert(sunriseSunset, SUN_CLASS, s"SELECT FROM $SUN_CLASS WHERE location like '${sunriseSunset.location}' AND date = '${sunriseSunset.date}'")
    }
  }
  override def getWeatherStorage(): WeatherStorage = weatherStorage

  // attributes
  lazy val attributeStorage = new AttributeStorage {
    override def storeAttribute(key: String, `type`: String, value: String): Future[Unit] = {
      val attr = KeyValue(key, `type`, value)
      upsert(attr, ATTRIBUTE_CLASS, s"SELECT FROM $ATTRIBUTE_CLASS WHERE type = '${`type`}' and key = '$key'")
    }
    override def getAttribute(key: String, `type`: String): Future[Option[String]] = {
      queryForOption[KeyValue](s"SELECT FROM $ATTRIBUTE_CLASS WHERE type = '${`type`}' AND key = '$key'")
        .map(_.map(_.value))
    }
  }
  override def getAttributeStorage(): AttributeStorage = attributeStorage

  // various achievments
  lazy val achievementStorage = new AchievementStorage {
    object ResRow {
      implicit val maxRowFormat = Format[ResRow](Json.reads[ResRow], Json.writes[ResRow])
    }
    case class ResRow(res_value: Double)

    private def minOf(athleteId: Long, fieldName: String, mapperFunc: Activity => Option[Double], tolerance: Double = .1d): Future[Option[Achievement]] = {
      val result = for {
        minResult <- OptionT(queryForOption[ResRow](s"SELECT MIN($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride'"))
        _ = log.debug(s"min[$fieldName]=${minResult.res_value}")
        activity <- OptionT(queryForOption[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride' AND $fieldName <= ${minResult.res_value + tolerance} ORDER BY $fieldName ASC LIMIT 1"))
        minValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = minValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.run
    }

    private def maxOf(athleteId: Long, fieldName: String, mapperFunc: Activity => Option[Double], tolerance: Double = .1d): Future[Option[Achievement]] = {
      val result = for {
        maxResult <- OptionT(queryForOption[ResRow](s"SELECT MAX($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride'"))
        _ = log.debug(s"max[$fieldName]=${maxResult.res_value}")
        activity <- OptionT(queryForOption[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride' AND $fieldName >= ${maxResult.res_value - tolerance} ORDER BY $fieldName DESC LIMIT 1"))
        maxValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = maxValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.run
    }

    override def maxSpeed(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "max_speed", _.max_speed.map(_.toDouble))
    override def maxAverageSpeed(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "average_speed", _.average_speed.map(_.toDouble))
    override def maxDistance(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "distance", _.distance.toDouble.some)
    override def maxElevation(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "total_elevation_gain", _.total_elevation_gain.toDouble.some)
    override def maxHeartRate(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "max_heartrate", _.max_heartrate.map(_.toDouble))
    override def maxPower(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "max_watts", _.max_watts.map(_.toDouble))
    override def maxAveragePower(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "average_watts", _.average_watts.map(_.toDouble))
    override def minTemperature(athleteId: Long): Future[Option[Achievement]] = minOf(athleteId, "average_temp", _.average_temp.map(_.toDouble))
    override def maxTemperature(athleteId: Long): Future[Option[Achievement]] = maxOf(athleteId, "average_temp", _.average_temp.map(_.toDouble))
  }
  override def getAchievementStorage(): AchievementStorage = achievementStorage

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

      def createIxIfNeeded(className: String, index: IndexSetup*) {
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

      def dropIx(className: String, ixName: String) {
        val ixManager = odb.getMetadata.getIndexManager
        // old name was without hyphen, try both versions
        val names = Seq(s"$ixName$className", s"$ixName-$className")
        names.foreach{n =>
          if (ixManager.existsIndex(n)) ixManager.dropIndex(n)
        }
        val schema = odb.getMetadata.getSchema
        val clazz = schema.getClass(className)
        Option(clazz).foreach(_.dropProperty(ixName))
      }

      //dropIx(ACTIVITY_CLASS, "id") // needed once after changing ix type
      createIxIfNeeded(ACTIVITY_CLASS, IndexSetup("id", OType.LONG))
      //dropIx(ACCOUNT_CLASS, "athleteId") // needed once after changing ix type
      createIxIfNeeded(ACCOUNT_CLASS, IndexSetup("athleteId", OType.LONG))
      createIxIfNeeded(CLUB_CLASS, IndexSetup("id", OType.INTEGER))
      //dropIx(ATHLETE_CLASS, "id") // needed once after changing ix type
      createIxIfNeeded(ATHLETE_CLASS, IndexSetup("id", OType.LONG))
      createIxIfNeeded(WEATHER_CLASS, IndexSetup("location", OType.STRING), IndexSetup("timestamp", OType.LONG))
      createIxIfNeeded(SUN_CLASS, IndexSetup("location", OType.STRING), IndexSetup("date", OType.STRING))
      createIxIfNeeded(ATTRIBUTE_CLASS, IndexSetup("key", OType.STRING))
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
      override def onMessage(iText: String)= log.trace(s"backup $iText")
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

  // asynch
  private def queryFor[T](sql: String)(implicit fjs: Reads[T]): Future[Seq[T]] = inTx() { db =>
    Try {
      val promise = Promise[Seq[T]]()
      db.query(new OSQLNonBlockingQuery[ODocument](sql, new OCommandResultListener() {
        val accuResults = new ListBuffer[T]

        override def result(iRecord: Any): Boolean = {
          val doc = iRecord.asInstanceOf[ODocument]
          accuResults += JsonIo.read[T](doc.toJSON)
          true
        }

        override def end() {
          // might be called twice in case of failure
          if (!promise.isCompleted) {
            promise.success(accuResults)
          }
        }

        override def getResult: AnyRef = accuResults
      }))
      promise.future
    }.fold(err => Future.failed(err), res => res)
  }

  private def queryForOption[T](sql: String)(implicit fjs: Reads[T]): Future[Option[T]] = queryFor(sql).map(_.headOption)

  private def upsert[T](payload: T, className: String, sql: String)(implicit fjs: Writes[T]): Future[Unit] = inTx() { db =>
    Try {
      val promise = Promise[Unit]()
      db.query(new OSQLNonBlockingQuery[ODocument](sql, new OCommandResultListener() {
        val accuResults = new ListBuffer[ODocument]

        override def result(iRecord: Any): Boolean = {
          val doc = iRecord.asInstanceOf[ODocument]
          accuResults += doc
          false
        }

        override def end() {
          val doc = accuResults.headOption.getOrElse(new ODocument(className))
          doc.fromJSON(JsonIo.write(payload))
          doc.save()
          promise.success(Unit)
        }

        override def getResult: AnyRef = null
      }))
      promise.future
    }.fold(err => Future.failed(err), res => res)
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
  val SUN_CLASS = "Sun"
  val ATTRIBUTE_CLASS = "Attribute"

}
