package velocorner.manual.storage

import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import zio.{ExitCode, URIO, ZIO}

object ActivitiesFromStorageApp extends zio.App with AggregateActivities with MyMacConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = for {
      storage <- ZIO.effect(Storage.create("or"))
      _ <- ZIO.effect(storage.initialize())
      progress <- ZIO.fromFuture(_ => storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
      _ <- ZIO.effect(printAllProgress(progress))
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.fold(_ => ExitCode.failure, _ => ExitCode.success)
  }

}
