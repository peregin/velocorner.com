package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport
import zio.{ExitCode, URIO, ZIO}

object AchievementsFromStorageApp extends zio.App with LazyLogging with FlywaySupport with MyLocalConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = for {
      storage <- ZIO.effect(localPsqlDb)
      _ <- ZIO.effect(storage.initialize())
      maxAchievement <- ZIO.fromFuture(global =>
        storage.getAchievementStorage.maxAverageHeartRate(9463742, "Ride").recover { case _ => None }(global)
      )
      _ <- ZIO.effect(logger.info(s"max achievement ${maxAchievement.toString}"))
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.fold(_ => ExitCode.failure, _ => ExitCode.success)
  }
}
