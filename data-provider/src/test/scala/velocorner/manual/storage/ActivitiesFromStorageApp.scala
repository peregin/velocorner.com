package velocorner.manual.storage

import cats.effect.{IO, IOApp}
import velocorner.manual.{AggregateActivities, MyLocalConfig}
import velocorner.model.DailyProgress
import velocorner.storage.Storage

object ActivitiesFromStorageApp extends IOApp.Simple with AggregateActivities with MyLocalConfig {

  override def run: IO[Unit] = for {
    storage <- IO(Storage.create("or"))
    _ <- IO.delay(storage.initialize())
    progress <- IO.fromFuture(IO(storage.listAllActivities(432909, "Ride"))).map(DailyProgress.from)
    _ <- IO.delay(printAllProgress(progress))
    _ <- IO.delay(storage.destroy())
  } yield ()
}
