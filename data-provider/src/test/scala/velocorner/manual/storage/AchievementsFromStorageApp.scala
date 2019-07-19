package velocorner.manual.storage

import org.slf4s.Logging
import scalaz.zio._
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.Storage


object AchievementsFromStorageApp extends App with Logging with AwaitSupport with MyMacConfig {

  override def run(args: List[String]): ZIO[AchievementsFromStorageApp.Environment, Nothing, Int] = {
    val res = for {
      storage <- ZIO.succeed(Storage.create("or"))
      _ <- ZIO.apply(storage.initialize())
      maxAchievement <- ZIO.fromFuture(global => storage.getAchievementStorage().maxAverageHeartRate(9463742).recover{case _ => None}(global))
      _ = log.info(s"max achievement ${maxAchievement.toString}")
      _ <- ZIO.apply(storage.destroy())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}

