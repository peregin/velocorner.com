package velocorner.storage

import com.orientechnologies.orient.core.command.OCommandResultListener
import com.orientechnologies.orient.core.db.{ODatabaseType, OrientDB, OrientDBConfig}
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLNonBlockingQuery
import play.api.libs.json.{Format, Json, Reads, Writes}
import velocorner.model._
import velocorner.model.strava.Club
import velocorner.storage.OrientDbStorage._
import velocorner.util.{CloseableResource, JsonIo, Metrics}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.Try
import scala.util.control.Exception._
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging
import scalaz._
import scalaz.std.list._
import scalaz.syntax.std.option._
import scalaz.syntax.std.boolean._
import scalaz.std.scalaFuture._
import scalaz.syntax.traverse.ToTraverseOps
import velocorner.api.{Achievement, Activity, Athlete}
import velocorner.api.weather.{SunriseSunset, WeatherForecast}

import scala.jdk.CollectionConverters._

/**
 * Created by levi on 14.11.16.
 */
class OrientDbStorage(dbUrl: Option[String], dbPassword: String)
  extends Storage[Future] with CloseableResource with Metrics with LazyLogging {

  @volatile var server: Option[OrientDB] = None
  private val dbUser = dbUrl.isDefined ? "root" | "admin"
  private val orientDbUrl = dbUrl.map("remote:" + _).getOrElse("memory:")
  private val dbType = dbUrl.isDefined ? ODatabaseType.PLOCAL | ODatabaseType.MEMORY

  private def lookup[T](className: String, propertyName: String, propertyValue: Long)(implicit fjs: Reads[T]): Future[Option[T]] = {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    queryForOption[T](sql)
  }

  // TODO: workaround until elastic is in place
  def suggestActivities(snippet: String, athleteId: Long, max: Int): Future[Iterable[Activity]] = {
    queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' AND athlete.id = $athleteId AND name.toLowerCase() like '%${snippet.toLowerCase}%' ORDER BY start_date DESC LIMIT $max")
  }

  // to have an option list all rides for an athlete
  def listActivities(athleteId: Long, activityType: Option[String]): Future[Iterable[Activity]] = {
    val typeClause = activityType.map(a => s"type = '$a' AND ").getOrElse("")
    queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE $typeClause athlete.id = $athleteId ORDER BY start_date DESC")
  }

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = {
    activities
      .toList
      .traverseU(a => upsert(a, ACTIVITY_CLASS, s"SELECT FROM $ACTIVITY_CLASS WHERE id = ${a.id}"))
      .map(_ => ())
  }

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = Future { inTx { db =>
    val results = db.query(s"SELECT type AS name, COUNT(*) AS counter FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId GROUP BY name ORDER BY counter DESC")
    results.asScala.map(d => JsonIo.read[Counter](d.toJSON)).map(_.name).to(Iterable)
  }}

  override def dailyProgressForAthlete(athleteId: Long, activityType: String): Future[Iterable[DailyProgress]] = for {
    activities <- queryFor[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType'")
    _ = logger.debug(s"found activities ${activities.size} for $athleteId")
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

    object ResDoubleRow {
      implicit val doubleRowFormat = Format[ResDoubleRow](Json.reads[ResDoubleRow], Json.writes[ResDoubleRow])
    }
    case class ResDoubleRow(res_value: Double)

    object ResLongRow {
      implicit val longRowFormat = Format[ResLongRow](Json.reads[ResLongRow], Json.writes[ResLongRow])
    }
    case class ResLongRow(res_value: Long)

    private def minOf(athleteId: Long, activityType: String, fieldName: String, mapperFunc: Activity => Option[Double], tolerance: Double = .1d): Future[Option[Achievement]] = {
      val result = for {
        _ <- OptionT(queryForOption[ResLongRow](s"SELECT COUNT($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName IS NOT NULL"))
          .filter(_.res_value > 0L)
        minResult <- OptionT(queryForOption[ResDoubleRow](s"SELECT MIN($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType'"))
        _ = logger.debug(s"min[$fieldName]=${minResult.res_value}")
        activity <- OptionT(queryForOption[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName <= ${minResult.res_value + tolerance} ORDER BY $fieldName ASC LIMIT 1"))
        minValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = minValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.run
    }

    private def maxOf(athleteId: Long, activityType: String, fieldName: String, mapperFunc: Activity => Option[Double], tolerance: Double = .1d): Future[Option[Achievement]] = {
      val result = for {
        _ <- OptionT(queryForOption[ResLongRow](s"SELECT COUNT($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName IS NOT NULL"))
          .filter(_.res_value > 0L)
        maxResult <- OptionT(queryForOption[ResDoubleRow](s"SELECT MAX($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType'"))
        _ = logger.debug(s"max[$fieldName]=${maxResult.res_value}")
        activity <- OptionT(queryForOption[Activity](s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName >= ${maxResult.res_value - tolerance} ORDER BY $fieldName DESC LIMIT 1"))
        maxValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = maxValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.run
    }

    override def maxAverageSpeed(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "average_speed", _.average_speed.map(_.toDouble))
    override def maxDistance(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "distance", _.distance.toDouble.some)
    override def maxElevation(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "total_elevation_gain", _.total_elevation_gain.toDouble.some)
    override def maxHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "max_heartrate", _.max_heartrate.map(_.toDouble))
    override def maxAverageHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "average_heartrate", _.average_heartrate.map(_.toDouble))
    override def maxAveragePower(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "average_watts", _.average_watts.map(_.toDouble))
    override def minTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] = minOf(athleteId, activity, "average_temp", _.average_temp.map(_.toDouble))
    override def maxTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] = maxOf(athleteId, activity, "average_temp", _.average_temp.map(_.toDouble))
  }
  override def getAchievementStorage(): AchievementStorage = achievementStorage

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize(): Unit = {
    val orientDb: OrientDB = new OrientDB(orientDbUrl, dbUser, dbPassword, OrientDBConfig.defaultConfig())
    orientDb.createIfNotExists(DATABASE_NAME, dbType)
    server = orientDb.some

    inTx { odb =>
      case class IndexSetup(indexField: String, indexType: OType)

      def createIxIfNeeded(className: String, index: IndexSetup*): Unit = {
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

      def dropIx(className: String, ixName: String): Unit = {
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

      createIxIfNeeded(ACTIVITY_CLASS, IndexSetup("id", OType.LONG))
      createIxIfNeeded(ACCOUNT_CLASS, IndexSetup("athleteId", OType.LONG))
      createIxIfNeeded(CLUB_CLASS, IndexSetup("id", OType.INTEGER))
      createIxIfNeeded(ATHLETE_CLASS, IndexSetup("id", OType.LONG))
      createIxIfNeeded(WEATHER_CLASS, IndexSetup("location", OType.STRING), IndexSetup("timestamp", OType.LONG))
      createIxIfNeeded(SUN_CLASS, IndexSetup("location", OType.STRING), IndexSetup("date", OType.STRING))
      createIxIfNeeded(ATTRIBUTE_CLASS, IndexSetup("key", OType.STRING))
    }
  }

  // releases any connections, resources used
  override def destroy(): Unit = {
    server.foreach(_.close())
    server = None
    logger.info("database has been closed...")
  }

  def inTx[T](body: ODatabaseDocument => T): T = {
    server.map { orientDb =>
      val session = orientDb.open(DATABASE_NAME, dbUser, dbPassword)
      session.activateOnCurrentThread()
      ultimately {
        session.close()
      }.apply {
        body(session)
      }
    }.getOrElse(throw new IllegalStateException("database is closed"))
  }

  // asynch
  private def queryFor[T](sql: String)(implicit fjs: Reads[T]): Future[Seq[T]] = inTx { db =>
    Try {
      val promise = Promise[Seq[T]]()
      db.query(new OSQLNonBlockingQuery[ODocument](sql, new OCommandResultListener() {
        val accuResults = new ListBuffer[T]

        override def result(iRecord: Any): Boolean = {
          val doc = iRecord.asInstanceOf[ODocument]
          accuResults += JsonIo.read[T](doc.toJSON)
          true
        }

        override def end(): Unit = {
          // might be called twice in case of failure
          if (!promise.isCompleted) {
            promise.success(accuResults.toSeq)
          }
        }

        override def getResult: AnyRef = accuResults
      }))
      promise.future
    }.fold(err => Future.failed(err), res => res)
  }

  private def queryForOption[T](sql: String)(implicit fjs: Reads[T]): Future[Option[T]] = queryFor(sql).map(_.headOption)

  private def upsert[T](payload: T, className: String, sql: String)(implicit fjs: Writes[T]): Future[Unit] = inTx { db =>
    Try {
      val promise = Promise[Unit]()
      db.query(new OSQLNonBlockingQuery[ODocument](sql, new OCommandResultListener() {
        val accuResults = new ListBuffer[ODocument]

        override def result(iRecord: Any): Boolean = {
          val doc = iRecord.asInstanceOf[ODocument]
          accuResults += doc
          false
        }

        override def end(): Unit = {
          val doc = accuResults.headOption.getOrElse(new ODocument(className))
          doc.fromJSON(JsonIo.write(payload))
          doc.save()
          promise.success(())
        }

        override def getResult: AnyRef = null
      }))
      promise.future
    }.fold(err => Future.failed(err), res => res)
  }
}

object OrientDbStorage {

  val DATABASE_NAME = "velocorner"

  val ACTIVITY_CLASS = "Activity"
  val ACCOUNT_CLASS = "Account"
  val CLUB_CLASS = "Club"
  val ATHLETE_CLASS = "Athlete"
  val WEATHER_CLASS = "Weather"
  val SUN_CLASS = "Sun"
  val ATTRIBUTE_CLASS = "Attribute"

}
