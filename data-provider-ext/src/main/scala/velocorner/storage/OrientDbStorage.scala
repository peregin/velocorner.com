package velocorner.storage

import cats.data.OptionT
import cats.implicits._
import com.orientechnologies.orient.core.command.OCommandResultListener
import com.orientechnologies.orient.core.config.OGlobalConfiguration
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.db.{ODatabasePool, ODatabaseType, OrientDB, OrientDBConfig}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLNonBlockingQuery
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, Reads, Writes}
import velocorner.api.Achievement
import velocorner.api.strava.Activity
import velocorner.model._
import velocorner.model.strava.Gear
import velocorner.storage.OrientDbStorage._
import velocorner.util.{CloseableResource, JsonIo, Metrics}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.Try
import scala.util.control.Exception._

object Counter {
  implicit val entryFormat = Format[Counter](Json.reads[Counter], Json.writes[Counter])
}

case class Counter(name: String, counter: Long)

/**
 * Created by levi on 14.11.16.
 * Improvements to do:
 * - use new query API from OrientDB 3.0 ?
 * - use compound index for athlete.id and activity type
 * - use monad stack M[_] : Monad
 */
//noinspection NotImplementedCode
class OrientDbStorage(url: Option[String], dbPassword: String) extends Storage[Future] with CloseableResource with Metrics with LazyLogging {

  @volatile private var server: Option[OrientDB] = None
  @volatile private var pool: Option[ODatabasePool] = None
  private val dbUser = url.map(_ => "root").getOrElse("admin")
  private val dbUrl = url.map("remote:" + _).getOrElse("memory:")
  private val dbType = url.map(_ => ODatabaseType.PLOCAL).getOrElse(ODatabaseType.MEMORY)

  private def lookup[T](className: String, propertyName: String, propertyValue: String)(implicit fjs: Reads[T]): Future[Option[T]] = {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    queryForOption[T](sql)
  }

  override def suggestActivities(snippet: String, athleteId: Long, max: Int): Future[Iterable[Activity]] =
    queryFor[Activity](
      s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' AND athlete.id = $athleteId AND name.toLowerCase() like '%${snippet.toLowerCase}%' ORDER BY start_date DESC LIMIT $max"
    )

  override def activityTitles(athleteId: Long, max: Int): Future[Iterable[String]] = ???

  // insert all activities, new ones are added, previous ones are overridden
  override def storeActivity(activities: Iterable[Activity]): Future[Unit] =
    activities.toList
      .traverse(a => upsert(a, ACTIVITY_CLASS, s"SELECT FROM $ACTIVITY_CLASS WHERE id = :id", Map("id" -> a.id)))
      .void

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = Future {
    transact { db =>
      val results = db.query(
        s"SELECT type AS name, COUNT(*) AS counter FROM $ACTIVITY_CLASS WHERE athlete.id = :id GROUP BY name ORDER BY counter DESC",
        Map(
          "id" -> athleteId
        ).asJava
      )
      results.asScala.map(d => JsonIo.read[Counter](d.toJSON)).map(_.name).to(Iterable)
    }
  }

  override def listActivityYears(athleteId: Long, activityType: String): Future[Iterable[Int]] = ???

  override def listAllActivities(athleteId: Long, activityType: String): Future[Iterable[Activity]] =
    queryFor[Activity](
      s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = :id AND type = :type",
      Map(
        "id" -> athleteId,
        "type" -> activityType
      )
    )

  override def listYtdActivities(athleteId: Long, activityType: String, year: Int): Future[Iterable[Activity]] = ???

  override def listActivities(athleteId: Long, from: DateTime, to: DateTime): Future[Iterable[Activity]] =
    queryFor[Activity](
      s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = :id AND date_from > :dateFrom AND date_from < :dateTo",
      Map(
        "id" -> athleteId,
        "dateFrom" -> from,
        "dateTo" -> to
      )
    )

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] =
    queryFor[Activity](
      s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = :id ORDER BY start_date DESC LIMIT :limit",
      Map(
        "id" -> athleteId,
        "limit" -> limit
      )
    )

  override def listTopActivities(athleteId: Long, actionType: ActionType.Entry, activityType: String, limit: Int): Future[Iterable[Activity]] = ???

  override def getLastActivity(athleteId: Long): Future[Option[Activity]] = ???

  override def getActivity(id: Long): Future[Option[Activity]] = lookup[Activity](ACTIVITY_CLASS, "id", id.toString)

  // accounts
  override def getAccountStorage: AccountStorage[Future] = accountStorage
  private lazy val accountStorage = new AccountStorage[Future] {
    override def store(account: Account): Future[Unit] =
      upsert(account, ACCOUNT_CLASS, s"SELECT FROM $ACCOUNT_CLASS WHERE athleteId = :id", Map("id" -> account.athleteId))
    override def getAccount(id: Long): Future[Option[Account]] = lookup[Account](ACCOUNT_CLASS, "athleteId", id.toString)
  }

  // gears
  override def getGearStorage: GearStorage[Future] = gearStorage
  private lazy val gearStorage = new GearStorage[Future] {
    override def store(gear: Gear, `type`: Gear.Entry, athleteId: Long): Future[Unit] =
      upsert(gear, GEAR_CLASS, s"SELECT FROM $GEAR_CLASS WHERE id = :id", Map("id" -> gear.id))
    override def getGear(id: String): Future[Option[Gear]] = lookup[Gear](GEAR_CLASS, "id", id)
    override def listGears(athleteId: Long): Future[Iterable[Gear]] = ???
  }

  // various achievements
  lazy val achievementStorage = new AchievementStorage[Future] {
    object ResDoubleRow {
      implicit val doubleRowFormat = Format[ResDoubleRow](Json.reads[ResDoubleRow], Json.writes[ResDoubleRow])
    }

    case class ResDoubleRow(res_value: Double)

    object ResLongRow {
      implicit val longRowFormat = Format[ResLongRow](Json.reads[ResLongRow], Json.writes[ResLongRow])
    }

    case class ResLongRow(res_value: Long)

    private def minOf(
        athleteId: Long,
        activityType: String,
        fieldName: String,
        mapperFunc: Activity => Option[Double],
        tolerance: Double = .1d
    ): Future[Option[Achievement]] = {
      val result = for {
        _ <- OptionT(
          queryForOption[ResLongRow](
            s"SELECT COUNT($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName IS NOT NULL"
          )
        )
          .filter(_.res_value > 0L)
        minResult <- OptionT(
          queryForOption[ResDoubleRow](
            s"SELECT MIN($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType'"
          )
        )
        _ = logger.debug(s"min[$fieldName]=${minResult.res_value}")
        activity <- OptionT(
          queryForOption[Activity](
            s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName <= ${minResult.res_value + tolerance} ORDER BY $fieldName ASC LIMIT 1"
          )
        )
        minValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = minValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.value
    }

    private def maxOf(
        athleteId: Long,
        activityType: String,
        fieldName: String,
        mapperFunc: Activity => Option[Double],
        tolerance: Double = .1d
    ): Future[Option[Achievement]] = {
      val result = for {
        _ <- OptionT(
          queryForOption[ResLongRow](
            s"SELECT COUNT($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName IS NOT NULL"
          )
        )
          .filter(_.res_value > 0L)
        maxResult <- OptionT(
          queryForOption[ResDoubleRow](
            s"SELECT MAX($fieldName) AS res_value FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType'"
          )
        )
        _ = logger.debug(s"max[$fieldName]=${maxResult.res_value}")
        activity <- OptionT(
          queryForOption[Activity](
            s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = '$activityType' AND $fieldName >= ${maxResult.res_value - tolerance} ORDER BY $fieldName DESC LIMIT 1"
          )
        )
        maxValue <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = maxValue,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.value
    }

    override def maxAverageSpeed(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "average_speed", _.average_speed.map(_.toDouble))

    override def maxDistance(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "distance", _.distance.toDouble.some)

    override def maxTime(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "moving_time", _.distance.toDouble.some)

    override def maxElevation(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "total_elevation_gain", _.total_elevation_gain.toDouble.some)

    override def maxHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "max_heartrate", _.max_heartrate.map(_.toDouble))

    override def maxAverageHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "average_heartrate", _.average_heartrate.map(_.toDouble))

    override def maxAveragePower(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "average_watts", _.average_watts.map(_.toDouble))

    override def minAverageTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] =
      minOf(athleteId, activity, "average_temp", _.average_temp.map(_.toDouble))

    override def maxAverageTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] =
      maxOf(athleteId, activity, "average_temp", _.average_temp.map(_.toDouble))
  }

  override def getAchievementStorage: AchievementStorage[Future] = achievementStorage

  override def getAdminStorage: AdminStorage[Future] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize(): Unit = {
    val config = OrientDBConfig
      .builder()
      .addConfig(OGlobalConfiguration.DB_POOL_MIN, 2)
      .addConfig(OGlobalConfiguration.DB_POOL_MAX, 10)
      .addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, true)
      .build()
    val orientDb: OrientDB = new OrientDB(dbUrl, dbUser, dbPassword, config)
    orientDb.createIfNotExists(DATABASE_NAME, dbType)

    server = orientDb.some
    pool = new ODatabasePool(orientDb, DATABASE_NAME, dbUser, dbPassword, config).some

    transact { odb =>
      case class IndexSetup(indexField: String, indexType: OType)

      def createIxIfNeeded(className: String, indexType: OClass.INDEX_TYPE, index: IndexSetup*): Unit = {
        val schema = odb.getMetadata.getSchema
        if (!schema.existsClass(className)) schema.createClass(className)
        val clazz = schema.getClass(className)

        index.foreach(ix => if (!clazz.existsProperty(ix.indexField)) clazz.createProperty(ix.indexField, ix.indexType))

        val ixFields = index.map(_.indexField).sorted
        val ixName = ixFields.mkString("-").replace(".", "_")

        if (!clazz.areIndexed(ixFields: _*)) clazz.createIndex(s"$ixName-$className", indexType, ixFields: _*)
      }

      def dropIx(className: String, ixName: String): Unit = {
        val ixManager = odb.getMetadata.getIndexManager
        // old name was without hyphen, try both versions
        val names = Seq(s"$ixName$className", s"$ixName-$className")
        names.foreach { n =>
          if (ixManager.existsIndex(n)) ixManager.dropIndex(n)
        }
        val schema = odb.getMetadata.getSchema
        val clazz = schema.getClass(className)
        Option(clazz).foreach(_.dropProperty(ixName))
      }

      createIxIfNeeded(ACTIVITY_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("id", OType.LONG))
      createIxIfNeeded(ACTIVITY_CLASS, OClass.INDEX_TYPE.NOTUNIQUE, IndexSetup("type", OType.STRING))
      createIxIfNeeded(ACTIVITY_CLASS, OClass.INDEX_TYPE.NOTUNIQUE, IndexSetup("athlete.id", OType.LONG))
      createIxIfNeeded(ACCOUNT_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("athleteId", OType.LONG))
      createIxIfNeeded(GEAR_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("id", OType.INTEGER))
      createIxIfNeeded(WEATHER_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("location", OType.STRING), IndexSetup("timestamp", OType.LONG))
      createIxIfNeeded(SUN_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("location", OType.STRING), IndexSetup("date", OType.STRING))
      createIxIfNeeded(ATTRIBUTE_CLASS, OClass.INDEX_TYPE.UNIQUE, IndexSetup("key", OType.STRING))

      odb.browseClass(ACCOUNT_CLASS).setFetchPlan("*:0")
      odb.browseClass(ACTIVITY_CLASS).setFetchPlan("*:0")
      odb.browseClass(WEATHER_CLASS).setFetchPlan("*:0")
      odb.browseClass(SUN_CLASS).setFetchPlan("*:0")
      odb.browseClass(ATTRIBUTE_CLASS).setFetchPlan("*:0")
    }
  }

  // releases any connections, resources used
  override def destroy(): Unit = {
    pool.foreach(_.close())
    pool = None
    server.foreach(_.close())
    server = None
    logger.info("database has been closed...")
  }

  private def transact[T](body: ODatabaseDocument => T): T =
    pool
      .map { dbPool =>
        val session = dbPool.acquire()
        session.activateOnCurrentThread()
        ultimately {
          session.close()
        }.apply {
          body(session)
        }
      }
      .getOrElse(throw new IllegalStateException("database is closed"))

  // asynch
  def queryFor[T](sql: String, args: Map[String, Any] = Map.empty)(implicit fjs: Reads[T]): Future[Seq[T]] = transact { db =>
    Try {
      val promise = Promise[Seq[T]]()
      db.query(
        new OSQLNonBlockingQuery[ODocument](
          sql,
          new OCommandResultListener() {
            val accuResults = new ListBuffer[T]

            override def result(iRecord: Any): Boolean = {
              val doc = iRecord.asInstanceOf[ODocument]
              accuResults += JsonIo.read[T](doc.toJSON)
              true
            }

            override def end(): Unit =
              // might be called twice in case of failure
              if (!promise.isCompleted) {
                promise.success(accuResults.toSeq)
              }

            override def getResult: AnyRef = accuResults
          }
        ),
        args.asJava
      )
      promise.future
    }.fold(err => Future.failed(err), res => res)
  }

  private def queryForOption[T](sql: String, args: Map[String, Any] = Map.empty)(implicit fjs: Reads[T]): Future[Option[T]] =
    queryFor(sql, args).map(_.headOption)

  private def upsert[T](payload: T, className: String, sql: String, args: Map[String, Any] = Map.empty)(implicit
      fjs: Writes[T]
  ): Future[Unit] = transact { db =>
    Try {
      val promise = Promise[Unit]()
      db.query(
        new OSQLNonBlockingQuery[ODocument](
          sql,
          new OCommandResultListener() {
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
          }
        ),
        args.asJava
      )
      promise.future
    }.fold(err => Future.failed(err), res => res)
  }
}

object OrientDbStorage {

  private val DATABASE_NAME = "velocorner"

  val ACTIVITY_CLASS = "Activity"
  val ACCOUNT_CLASS = "Account"
  val GEAR_CLASS = "Gear"
  val WEATHER_CLASS = "Weather"
  val SUN_CLASS = "Sun"
  val ATTRIBUTE_CLASS = "Attribute"

}
