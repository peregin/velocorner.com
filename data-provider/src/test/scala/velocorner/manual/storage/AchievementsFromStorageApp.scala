package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.Storage
import zio.ZIO


object AchievementsFromStorageApp extends zio.App with LazyLogging with AwaitSupport with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      storage <- ZIO.succeed(Storage.create("or"))
      _ <- ZIO.apply(storage.initialize())
      maxAchievement <- ZIO.fromFuture(
        global => storage.getAchievementStorage().maxAverageHeartRate(9463742, "Ride").recover{case _ => None}(global)
      )
      _ = logger.info(s"max achievement ${maxAchievement.toString}")
      _ <- ZIO.apply(storage.destroy())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}

