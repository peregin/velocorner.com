package velocorner.manual.storage

import velocorner.manual.{AggregateActivities, MyMacConfig, OpenWeatherApp}
import velocorner.storage.Storage
import zio.ZIO

object ActivitiesFromStorageApp extends zio.App with AggregateActivities with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      storage <- ZIO.apply(Storage.create("or"))
      _ <- ZIO.apply(storage.initialize())
      progress <- ZIO.fromFuture(_ => storage.dailyProgressForAthlete(432909, "Ride"))
      _ = printAllProgress(progress)
      _ <- ZIO.apply(storage.destroy())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }

}
