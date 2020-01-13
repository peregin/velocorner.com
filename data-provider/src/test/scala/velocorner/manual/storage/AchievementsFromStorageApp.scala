package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.MyMacConfig
import velocorner.storage.Storage
import zio.ZIO


object AchievementsFromStorageApp extends zio.App with LazyLogging with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      storage <- ZIO.effect(Storage.create("or"))
      _ <- ZIO.effect(storage.initialize())
      maxAchievement <- ZIO.fromFuture(
        global => storage.getAchievementStorage.maxAverageHeartRate(9463742, "Ride").recover{case _ => None}(global)
      )
      _ <- ZIO.effect(logger.info(s"max achievement ${maxAchievement.toString}"))
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}

