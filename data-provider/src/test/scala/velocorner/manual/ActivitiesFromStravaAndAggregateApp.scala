package velocorner.manual

import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import zio.{ExitCode, Task, URIO}
import zio.logging._

object ActivitiesFromStravaAndAggregateApp extends zio.App with AggregateActivities with MyLocalConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = Task
      .effect {
        val config = SecretConfig.load()
        new StravaActivityFeed(None, config)
      }
      .bracket { feed =>
        Task.effectTotal {
          feed.close()
          HttpFeed.shutdown()
        }
      } { implicit feed =>
        for {
          storage <- Task.effect(Storage.create("or"))
          _ <- Task.effect(storage.initialize())
          activities <- Task.fromFuture(_ => StravaActivityFeed.listRecentAthleteActivities)
          _ <- log.info(s"retrieved ${activities.size} activities")
          progress <- Task.fromFuture(_ => storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
          _ <- Task.effectTotal(printAllProgress(progress))
          _ <- Task.effectTotal(storage.destroy())
        } yield ()
      }
    res.provideLayer(zEnv).exitCode
  }
}
