package velocorner.storage

import java.util.concurrent.Executors

import cats.data.OptionT
import cats.effect.{Blocker, IO}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.implicits._
import doobie.{ConnectionIO, _}
import org.flywaydb.core.Flyway
import org.postgresql.util.PGobject
import play.api.libs.json.{Reads, Writes}
import velocorner.api.{Achievement, Activity}
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.model.Account
import velocorner.util.JsonIo

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
  config.validate() // for printout

  private lazy val connectEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5, (r: Runnable) =>
    new Thread(r, "connect ec") <| (_.setDaemon(true))
  ))
  private lazy val blockingEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5, (r: Runnable) =>
    new Thread(r, "blocking ec") <| (_.setDaemon(true))
  ))
  private lazy val transactor = Transactor.fromDataSource[IO](
    dataSource = new HikariDataSource(config),
    connectEC = connectEC,
    blocker = Blocker.liftExecutionContext(blockingEC)
  )

  def playJsonMeta[A: Reads : Writes : Manifest]: Meta[A] = Meta
    .Advanced.other[PGobject]("jsonb")
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
  private implicit val weatherMeta: Meta[WeatherForecast] = playJsonMeta[WeatherForecast]
  private implicit val sunMeta: Meta[SunriseSunset] = playJsonMeta[SunriseSunset]

  implicit class ConnectionIOOps[T](cio: ConnectionIO[T]) {
    def toFuture: Future[T] = cio.transact(transactor).unsafeToFuture()
  }

  // to access it from the migration
  def toFuture[T](cio: ConnectionIO[T]): Future[T] = cio.toFuture

  override def storeActivity(activities: Iterable[Activity]): Future[Unit] =
    activities.map { a =>
      sql"""insert into activity (id, type, athlete_id, data)
           |values(${a.id}, ${a.`type`}, ${a.athlete.id}, $a) on conflict(id)
           |do update set type = ${a.`type`}, athlete_id = ${a.athlete.id}, data = $a
           |""".stripMargin.update.run.void
    }.toList.traverse(identity).void.toFuture

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] =
    sql"""select type as name, count(*) as counter from activity
         |where athlete_id = $athleteId
         |group by name
         |order by counter desc
         |""".stripMargin.query[String].to[List].toFuture

  override def listAllActivities(athleteId: Long, activityType: String): Future[Iterable[Activity]] =
    sql"""select data from activity
         |where athlete_id = $athleteId and type = $activityType
         |""".stripMargin.query[Activity].to[List].toFuture


  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] =
    sql"""select data from activity
         |where athlete_id = $athleteId limit $limit
         |""".stripMargin.query[Activity].to[List].toFuture

  override def suggestActivities(snippet: String, athleteId: Long, max: Int): Future[Iterable[Activity]] = {
    val searchPattern = "%" + snippet.toLowerCase + "%"
    sql"""select data from activity
         |where athlete_id = $athleteId and lower(data->>'name') like $searchPattern
         |order by data->>'start_date' desc
         |limit $max
         |""".stripMargin.query[Activity].to[List].toFuture
  }

  override def getActivity(id: Long): Future[Option[Activity]] =
    sql"""select data from activity where id = $id
         |""".stripMargin.query[Activity].option.toFuture


  override def getAccountStorage: AccountStorage = accountStorage

  private lazy val accountStorage = new AccountStorage {
    override def store(a: Account): Future[Unit] =
      sql"""insert into account (athlete_id, data)
           |values(${a.athleteId}, $a) on conflict(athlete_id)
           |do update set data = $a
           |""".stripMargin.update.run.void.toFuture

    override def getAccount(id: Long): Future[Option[Account]] =
      sql"""select data from account where athlete_id = $id
           |""".stripMargin.query[Account].option.toFuture
  }

  // not used anymore
  override def getAthleteStorage: AthleteStorage = ???

  // not used anymore
  override def getClubStorage: ClubStorage = ???

  override def getWeatherStorage: WeatherStorage = weatherStorage

  private lazy val weatherStorage = new WeatherStorage {
    override def listRecentForecast(location: String, limit: Int): Future[Iterable[WeatherForecast]] =
      sql"""select data from weather
           |where location = $location
           |order by update_time desc
           |limit $limit
           |""".stripMargin.query[WeatherForecast].to[List].toFuture

    override def storeWeather(forecast: Iterable[WeatherForecast]): Future[Unit] =
      forecast.map { a =>
        sql"""insert into weather (location, update_time, data)
             |values(${a.location}, ${a.timestamp}, $a) on conflict(location, update_time)
             |do update set data = $a
             |""".stripMargin.update.run.void
      }.toList.traverse(identity).void.toFuture

    override def getSunriseSunset(location: String, localDate: String): Future[Option[SunriseSunset]] =
      sql"""select data from sun where location = $location and update_date = $localDate
           |""".stripMargin.query[SunriseSunset].option.toFuture

    override def storeSunriseSunset(sunriseSunset: SunriseSunset): Future[Unit] =
      sql"""insert into sun (location, update_date, data)
           |values(${sunriseSunset.location}, ${sunriseSunset.date}, $sunriseSunset) on conflict(location, update_date)
           |do update set data = $sunriseSunset
           |""".stripMargin.update.run.void.toFuture
  }

  override def getAttributeStorage: AttributeStorage = attributeStorage

  private lazy val attributeStorage = new AttributeStorage {
    override def storeAttribute(key: String, `type`: String, value: String): Future[Unit] =
      sql"""insert into attribute (key, type, value)
           |values($key, ${`type`}, $value) on conflict(key, type)
           |do update set value = $value
           |""".stripMargin.update.run.void.toFuture

    override def getAttribute(key: String, `type`: String): Future[Option[String]] =
      sql"""select value from attribute where key = $key and type = ${`type`}
           |""".stripMargin.query[String].option.toFuture
  }

  override def getAchievementStorage: AchievementStorage = achievementStorage

  private lazy val achievementStorage = new AchievementStorage {

    def metricOf(field: String, athleteId: Long, activityType: String, mapperFunc: Activity => Option[Double], max: Boolean = true): Future[Option[Achievement]] = {
      //implicit val han = LogHandler.jdkLogHandler
      val clause = s" and cast(data->>'$field' as numeric) is not null order by cast(data->>'$field' as numeric) ${if (max) "desc" else "asc"} limit 1"
      val fragment = fr"select data from activity where athlete_id = $athleteId and type = $activityType" ++
        Fragment.const(clause)
      val result = for {
        activity <- OptionT(fragment.query[Activity].option.toFuture)
        metric <- OptionT(Future(mapperFunc(activity)))
      } yield Achievement(
        value = metric,
        activityId = activity.id,
        activityName = activity.name,
        activityTime = activity.start_date
      )
      result.value
    }

    override def maxAverageSpeed(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("average_speed", athleteId, activity, _.average_speed.map(_.toDouble))

    override def maxDistance(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("distance", athleteId, activity, _.distance.toDouble.some)

    override def maxElevation(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("total_elevation_gain", athleteId, activity, _.total_elevation_gain.toDouble.some)

    override def maxHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("max_heartrate", athleteId, activity, _.max_heartrate.map(_.toDouble))

    override def maxAverageHeartRate(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("average_heartrate", athleteId, activity, _.average_heartrate.map(_.toDouble))

    override def maxAveragePower(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("average_watts", athleteId, activity, _.average_watts.map(_.toDouble))

    override def minAverageTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("average_temp", athleteId, activity, _.average_temp.map(_.toDouble), max = false)

    override def maxAverageTemperature(athleteId: Long, activity: String): Future[Option[Achievement]] =
      metricOf("average_temp", athleteId, activity, _.average_temp.map(_.toDouble))
  }

  override def initialize(): Unit = {
    val flyway = Flyway.configure().locations("psql/migration").dataSource(dbUrl, dbUser, dbPassword).load()
    flyway.migrate()
  }

  override def destroy(): Unit = {
  }
}
