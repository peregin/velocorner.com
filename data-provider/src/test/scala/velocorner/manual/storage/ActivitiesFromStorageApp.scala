package velocorner.manual.storage

import velocorner.manual.{AggregateActivities, MyLocalConfig}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import zio.{Scope, ZIO, ZIOAppArgs}

object ActivitiesFromStorageApp extends zio.ZIOAppDefault with AggregateActivities with MyLocalConfig {

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    for {
      storage <- ZIO.attempt(Storage.create("or"))
      _ <- ZIO.attempt(storage.initialize())
      progress <- ZIO.fromFuture(_ => storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
      _ <- ZIO.succeed(printAllProgress(progress))
      _ <- ZIO.attempt(storage.destroy())
    } yield ()
}
