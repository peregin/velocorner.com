package velocorner.manual

import cats.effect.{IO, IOApp}
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import scala.concurrent.ExecutionContext.Implicits.global

object ActivitiesFromStravaAndAggregateApp extends IOApp.Simple with AggregateActivities with MyLocalConfig {

  override def run: IO[Unit] = for {
    _ <- IO.bracketFull(_ => IO.delay(new StravaActivityFeed(None, SecretConfig.load())))(feed =>
      for {
        storage <- IO.delay(Storage.create("or"))
        _ = storage.initialize()
        activities <- IO.fromFuture(IO(StravaActivityFeed.listRecentAthleteActivities(feed)))
        _ <- IO.println(s"retrieved ${activities.size} activities")
        progress <- IO.fromFuture(IO(storage.listAllActivities(432909, "Ride").map(DailyProgress.from)))
        _ <- IO.delay(printAllProgress(progress))
        _ <- IO.delay(storage.destroy())
      } yield ()
    )((feed, _) =>
      IO.delay {
        feed.close()
        HttpFeed.shutdown()
      }
    )
  } yield ()
}
