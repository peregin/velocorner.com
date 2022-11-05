package velocorner.manual.storage

import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport
import zio.{Scope, ZIO, ZIOAppArgs}

object AchievementsFromStorageApp extends zio.ZIOAppDefault with FlywaySupport with MyLocalConfig {

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    for {
      storage <- ZIO.attempt(localPsqlDb)
      _ <- ZIO.attempt(storage.initialize())
      maxAchievement <- ZIO.fromFuture(global => storage.getAchievementStorage.maxAverageHeartRate(9463742, "Ride").recover { case _ => None }(global))
      _ <- zio.Console.printLine(s"max achievement ${maxAchievement.mkString}")
      _ <- ZIO.attempt(storage.destroy())
    } yield ()
}
