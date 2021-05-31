package velocorner.storage

import java.util.concurrent.Executors
import cats.data.OptionT
import cats.effect.{Blocker, IO}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.implicits._
import doobie.{ConnectionIO, _}
import velocorner.api.weather.CurrentWeather
import velocorner.model.ActionType
// needed for sql interpolator when filtering on start date
import doobie.postgres.implicits._
import org.flywaydb.core.Flyway
import org.joda.time.DateTime
import org.postgresql.util.PGobject
import play.api.libs.json.{Reads, Writes}
import velocorner.api.{Achievement, GeoPosition}
import velocorner.api.strava.Activity
import velocorner.api.weather.WeatherForecast
import velocorner.model.Account
import velocorner.model.strava.Gear
import velocorner.util.JsonIo

import java.time.ZoneId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

// for kestrel combinator
import mouse.all._

class PsqlDbStorage(dbUrl: String, dbUser: String, dbPassword: String) extends Storage[Future] with LazyLogging {

  private implicit val cs = IO.contextShift(ExecutionContext.global)

  private val config = new HikariConfig()
  config.setDriverClassName("org.postgresql.Driver")
  config.setJdbcUrl(dbUrl)
  config.setUsername(dbUser)
  config.setPassword(dbPassword)
  config.setMaximumPoolSize(5)
  config.setMinimumIdle(2)
  config.setPoolName("hikari-db-pool")
  config.setAutoCommit(true)
  //config.validate() // for printout

  private lazy val connectEC = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(
      5,
      (r: Runnable) => new Thread(r, "connect ec") <| (_.setDaemon(true))
    )
  )
  private lazy val blockingEC = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(
      5,
      (r: Runnable) => new Thread(r, "blocking ec") <| (_.setDaemon(true))
    )
  )
  private lazy val transactor = Transactor.fromDataSource[IO](
    dataSource = new HikariDataSource(config),
    connectEC = connectEC,
    blocker = Blocker.liftExecutionContext(blockingEC)
  )

  def playJsonMeta[A: Reads: Writes: Manifest]: Meta[A] = Meta.Advanced
    .other[PGobject]("jsonb")
    .timap[A] { pgo =>
      JsonIo.read[A](pgo.getValue)
    } { json =>
      val o = new PGobject
      o.setType("jsonb")
      o.setValue(JsonIo.write[A](json))
      o
    }

  private implicit val activityMeta: Meta[Activity] = playJsonMeta[Activity]
  private implicit val accountMeta: Meta[Account] = playJsonMeta[Account]
  private implicit val forecastMeta: Meta[WeatherForecast] = playJsonMeta[WeatherForecast]
  private implicit val weatherMeta: Meta[CurrentWeather] = playJsonMeta[CurrentWeather]
  private implicit val gearMeta: Meta[Gear] = playJsonMeta[Gear]

  implicit class ConnectionIOOps[T](cio: ConnectionIO[T]) {
    def transactToFuture: Future[T] = cio.transact(transactor).unsafeToFuture()
  }

  // to access it from the migration
  def toFuture[T](cio: ConnectionIO[T]): Future[T] = cio.transactToFuture

  override def storeActivity(activities: Iterable[Activity]): Future[Unit] =
    activities
      .map { a =>
        sql"""insert into activity (id, type, athlete_id, data)
           |values(${a.id}, ${a.`type`}, ${a.athlete.id}, $a) on conflict(id)
           |do update set type = ${a.`type`}, athlete_id = ${a.athlete.id}, data = $a
           |""".stripMargin.update.run.void
      }
      .toList
      .traverse(identity)
      .void
      .transactToFuture

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] =
    sql"""select type as name, count(*) as counter from activity
         |where athlete_id = $athleteId
         |group by name
         |order by counter desc
         |""".stripMargin.query[String].to[List].transactToFuture

  override def listActivityYears(athleteId: Long, activityType: String): Future[Iterable[Int]] =
    sql"""select distinct extract(year from (data->>'start_date')::timestamp) as years from activity
         |where athlete_id = $athleteId and type = $activityType
         |order by years desc
         |""".stripMargin.query[Int].to[List].transactToFuture

  override def listAllActivities(
      athleteId: Long,
      activityType: String
  ): Future[Iterable[Activity]] =
    sql"""select data from activity
         |where athlete_id = $athleteId and type = $activityType
         |""".stripMargin.query[Activity].to[List].transactToFuture

  override def listYtdActivities(athleteId: Long, activityType: String, year: Int): Future[Iterable[Activity]] =
    sql"""select data from activity
         |where athlete_id = $athleteId and type = $activityType
         |and extract(year from (data->>'start_date')::timestamp) = $year
         |""".stripMargin.query[Activity].to[List].transactToFuture

  override def listActivities(
      athleteId: Long,
      from: DateTime,
      to: DateTime
  ): Future[Iterable[Activity]] = {
    val fromDate = java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(from.getMillis), ZoneId.of("UTC"))
    val toDate = java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(to.getMillis), ZoneId.of("UTC"))
    sql"""select data from activity
         |where athlete_id = $athleteId
         |and cast(data->>'start_date' as timestamp) > $fromDate
         |and cast(data->>'start_date' as timestamp) < $toDate
         |order by data->>'start_date' desc
         |""".stripMargin.query[Activity].to[List].transactToFuture
  }

  override def listRecentActivities(
      athleteId: Long,
      limit: Int
  ): Future[Iterable[Activity]] =
    sql"""select data from activity
         |where athlete_id = $athleteId
         |order by data->>'start_date' desc
         |limit $limit
         |""".stripMargin.query[Activity].to[List].transactToFuture


  // TODO[top10]: continue from here
  override def listTopActivities(athleteId: Long, actionType: ActionType.Entry, activityType: String, limit: Int): Future[Iterable[Activity]] = {
    val orderClause = "data->>'start_date'"
    sql"""select data from activity
         |where athlete_id = $athleteId and type = $activityType
         |order by $orderClause desc
         |limit $limit
         |""".stripMargin.query[Activity].to[List].transactToFuture
  }

  override def suggestActivities(
      snippet: String,
      athleteId: Long,
      max: Int
  ): Future[Iterable[Activity]] = {
    val searchPattern = "%" + snippet.toLowerCase + "%"
    sql"""select data from activity
         |where athlete_id = $athleteId and lower(data->>'name') like $searchPattern
         |order by data->>'start_date' desc
         |limit $max
         |""".stripMargin.query[Activity].to[List].transactToFuture
  }

  override def activitiesTitles(athleteId: Long, max: Int): Future[Iterable[String]] =
    sql"""select data->>'name' from activity
         |where athlete_id = $athleteId
         |order by data->>'start_date' desc
         |limit $max""".stripMargin.query[String].to[List].transactToFuture

  override def getActivity(id: Long): Future[Option[Activity]] =
    sql"""select data from activity where id = $id
         |""".stripMargin.query[Activity].option.transactToFuture

  override def getAccountStorage: AccountStorage = accountStorage

  private lazy val accountStorage = new AccountStorage {
    override def store(a: Account): Future[Unit] =
      sql"""insert into account (athlete_id, data)
           |values(${a.athleteId}, $a) on conflict(athlete_id)
           |do update set data = $a
           |""".stripMargin.update.run.void.transactToFuture

    override def getAccount(id: Long): Future[Option[Account]] =
      sql"""select data from account where athlete_id = $id
           |""".stripMargin.query[Account].option.transactToFuture
  }

  // gears
  override def getGearStorage: GearStorage = gearStorage

  private lazy val gearStorage = new GearStorage {
    override def store(gear: Gear, gearType: Gear.Entry): Future[Unit] =
      sql"""insert into gear (id, type, data)
           |values(${gear.id}, ${gearType.toString}, $gear) on conflict(id)
           |do update set data = $gear
           |""".stripMargin.update.run.void.transactToFuture

    override def getGear(id: String): Future[Option[Gear]] =
      sql"""select data from gear where id = $id
           |""".stripMargin.query[Gear].option.transactToFuture
  }

  override def getWeatherStorage: WeatherStorage = weatherStorage

  private lazy val weatherStorage = new WeatherStorage {
    override def listRecentForecast(
        location: String,
        limit: Int
    ): Future[Iterable[WeatherForecast]] =
      sql"""select data from forecast
           |where location = $location
           |order by update_time desc
           |limit $limit
           |""".stripMargin.query[WeatherForecast].to[List].transactToFuture

    override def storeRecentForecast(
        forecast: Iterable[WeatherForecast]
    ): Future[Unit] =
      forecast
        .map { a =>
          sql"""insert into forecast (location, update_time, data)
             |values(${a.location}, ${a.timestamp}, $a) on conflict(location, update_time)
             |do update set data = $a
             |""".stripMargin.update.run.void
        }
        .toList
        .traverse(identity)
        .void
        .transactToFuture

    override def suggestLocations(snippet: String): Future[Iterable[String]] = {
      val searchPattern = "%" + snippet.toLowerCase + "%"
      sql"""select distinct location from forecast
           |where lower(location) like $searchPattern
           |""".stripMargin.query[String].to[List].transactToFuture
    }

    override def getRecentWeather(location: String): Future[Option[CurrentWeather]] =
      sql"""select data from weather where location = $location
           |""".stripMargin.query[CurrentWeather].option.transactToFuture

    override def storeRecentWeather(
        weather: CurrentWeather
    ): Future[Unit] =
      sql"""insert into weather (location, data)
           |values(${weather.location}, $weather) on conflict(location)
           |do update set data = $weather
           |""".stripMargin.update.run.void.transactToFuture
  }

  override def getAttributeStorage: AttributeStorage[Future] = attributeStorage

  private lazy val attributeStorage = new AttributeStorage[Future] {
    override def storeAttribute(
        key: String,
        `type`: String,
        value: String
    ): Future[Unit] =
      sql"""insert into attribute (key, type, value)
           |values($key, ${`type`}, $value) on conflict(key, type)
           |do update set value = $value
           |""".stripMargin.update.run.void.transactToFuture

    override def getAttribute(
        key: String,
        `type`: String
    ): Future[Option[String]] =
      sql"""select value from attribute where key = $key and type = ${`type`}
           |""".stripMargin.query[String].option.transactToFuture
  }

  override def getAchievementStorage: AchievementStorage = achievementStorage

  private lazy val achievementStorage = new AchievementStorage {

    def metricOf(
        field: String,
        athleteId: Long,
        activityType: String,
        mapperFunc: Activity => Option[Double],
        max: Boolean = true
    ): Future[Option[Achievement]] = {
      //implicit val han = LogHandler.jdkLogHandler
      val clause =
        s" and cast(data->>'$field' as numeric) is not null order by cast(data->>'$field' as numeric) ${if (max) "desc"
        else "asc"} limit 1"
      val fragment =
        fr"select data from activity where athlete_id = $athleteId and type = $activityType" ++
          Fragment.const(clause)
      val result = for {
        activity <- OptionT(fragment.query[Activity].option.transactToFuture)
        metric <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = metric,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.value
    }

    override def maxAverageSpeed(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "average_speed",
        athleteId,
        activity,
        _.average_speed.map(_.toDouble)
      )

    override def maxDistance(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf("distance", athleteId, activity, _.distance.toDouble.some)

    override def maxElevation(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "total_elevation_gain",
        athleteId,
        activity,
        _.total_elevation_gain.toDouble.some
      )

    override def maxHeartRate(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "max_heartrate",
        athleteId,
        activity,
        _.max_heartrate.map(_.toDouble)
      )

    override def maxAverageHeartRate(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "average_heartrate",
        athleteId,
        activity,
        _.average_heartrate.map(_.toDouble)
      )

    override def maxAveragePower(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "average_watts",
        athleteId,
        activity,
        _.average_watts.map(_.toDouble)
      )

    override def minAverageTemperature(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "average_temp",
        athleteId,
        activity,
        _.average_temp.map(_.toDouble),
        max = false
      )

    override def maxAverageTemperature(
        athleteId: Long,
        activity: String
    ): Future[Option[Achievement]] =
      metricOf(
        "average_temp",
        athleteId,
        activity,
        _.average_temp.map(_.toDouble)
      )
  }

  override def getAdminStorage: AdminStorage = adminStorage
  lazy val adminStorage = new AdminStorage {
    override def countAccounts: Future[Long] = count("account")

    override def countActivities: Future[Long] = count("activity")

    private def count(table: String): Future[Long] = {
      val fr = fr"select count(*) from" ++ Fragment.const(table)
      fr.query[Long].unique.transactToFuture
    }

    override def countActiveAccounts: Future[Long] =
      sql"""select count(*) from account
           |where cast(data->>'lastUpdate' as timestamp) > current_date - interval '90' day
           |""".stripMargin.query[Long].unique.transactToFuture

  }

  override def getLocationStorage: LocationStorage = locationStorage
  lazy val locationStorage = new LocationStorage {
    override def store(
        location: String,
        position: GeoPosition
    ): Future[Unit] =
      sql"""insert into location (location, latitude, longitude)
           |values(${location.toLowerCase}, ${position.latitude}, ${position.longitude}) on conflict(location)
           |do update set latitude = ${position.latitude}, longitude = ${position.longitude}
           |""".stripMargin.update.run.void.transactToFuture

    override def getPosition(location: String): Future[Option[GeoPosition]] =
      sql"""select latitude, longitude from location where location = ${location.toLowerCase}""".stripMargin
        .query[(Double, Double)]
        .map(q => GeoPosition(q._1, q._2))
        .option
        .transactToFuture

    override def getCountry(ip: String): Future[Option[String]] =
      sql"""select country from ip2nation 
           |where ip < ((${ip.toLowerCase}::inet - '0.0.0.0'::inet)::numeric)
           |order by ip desc limit 1 """.stripMargin
        .query[String]
        .option
        .transactToFuture
  }

  override def initialize(): Unit = {
    val flyway = Flyway
      .configure()
      .locations("psql/migration")
      .dataSource(dbUrl, dbUser, dbPassword)
      .load()
    flyway.migrate()
  }

  override def destroy(): Unit = {}
}
