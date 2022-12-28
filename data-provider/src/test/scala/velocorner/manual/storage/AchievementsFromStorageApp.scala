package velocorner.manual.storage

import cats.effect.{IO, IOApp}
import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport

import scala.concurrent.ExecutionContext.Implicits.global

object AchievementsFromStorageApp extends IOApp.Simple with FlywaySupport with MyLocalConfig {

  override def run: IO[Unit] = for {
    storage <- IO(localPsqlDb)
    _ <- IO.delay(storage.initialize())
    maxAchievement <- IO.fromFuture(IO(storage.getAchievementStorage.maxAverageHeartRate(9463742, "Ride").recover { case _ => None }))
    _ <- IO.println(s"max achievement ${maxAchievement.mkString}")
    _ <- IO.delay(storage.destroy())
  } yield ()
}
