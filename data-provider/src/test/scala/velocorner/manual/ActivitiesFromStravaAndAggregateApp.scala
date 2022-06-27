package velocorner.manual

import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import zio.{Scope, ZIO, ZIOAppArgs}

object ActivitiesFromStravaAndAggregateApp extends zio.ZIOAppDefault with AggregateActivities with MyLocalConfig {

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    ZIO.acquireReleaseWith(
      ZIO.attempt(new StravaActivityFeed(None, SecretConfig.load()))
    ) { feed =>
      ZIO.succeed {
        feed.close()
        HttpFeed.shutdown()
      }
    } { implicit feed =>
      for {
        storage <- ZIO.attempt(Storage.create("or"))
        _ <- ZIO.succeed(storage.initialize())
        activities <- ZIO.fromFuture(_ => StravaActivityFeed.listRecentAthleteActivities)
        _ <- zio.Console.printLine(s"retrieved ${activities.size} activities")
        progress <- ZIO.fromFuture(_ => storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
        _ <- ZIO.succeed(printAllProgress(progress))
        _ <- ZIO.attempt(storage.destroy())
      } yield ()
    }
}
