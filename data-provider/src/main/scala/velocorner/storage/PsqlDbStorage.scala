package velocorner.storage

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway

import scala.concurrent.{ExecutionContext, Future}
import velocorner.api.{Activity, Athlete}
import velocorner.model.Account

class PsqlDbStorage(dbUrl: String, dbUser: String, dbPassword: String) extends Storage[Future] {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  lazy val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver", url = dbUrl, user = dbUser, pass = dbPassword
  )

  override def storeActivity(activities: Iterable[Activity]): Future[Unit] = ???

  override def listActivityTypes(athleteId: Long): Future[Iterable[String]] = ???

  override def listAllActivities(athleteId: Long, activityType: String): Future[Iterable[Activity]] = ???

  override def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]] = ???

  override def getActivity(id: Long): Future[Option[Activity]] = ???

  override def store(account: Account): Future[Unit] = ???

  override def getAccount(id: Long): Future[Option[Account]] = ???

  override def store(athlete: Athlete): Future[Unit] = ???

  override def getAthlete(id: Long): Future[Option[Athlete]] = ???

  override def getClubStorage: ClubStorage = ???

  override def getWeatherStorage: WeatherStorage = ???

  override def getAttributeStorage: AttributeStorage = ???

  override def getAchievementStorage: AchievementStorage = ???

  override def initialize(): Unit = {
    val flyway = Flyway.configure().locations("db/migration").dataSource(dbUrl, dbUser, dbPassword).load()
    flyway.migrate()
  }

  override def destroy(): Unit = {
  }
}
