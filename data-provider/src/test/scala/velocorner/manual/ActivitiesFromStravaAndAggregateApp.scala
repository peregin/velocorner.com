package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.DailyProgress
import velocorner.storage.Storage
import zio.{ExitCode, Task, URIO}

object ActivitiesFromStravaAndAggregateApp extends zio.App with LazyLogging with AggregateActivities with MyMacConfig {


  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = Task.effect{
      val config = SecretConfig.load()
      new StravaActivityFeed(None, config)
    }.bracket{ feed =>
      Task.effectTotal{
        feed.close()
        HttpFeed.shutdown()
      }
    }{ implicit feed =>
      for {
        storage <- Task.effect(Storage.create("or"))
        _ <- Task.effect(storage.initialize())
        activities <- Task.fromFuture(_ => StravaActivityFeed.listRecentAthleteActivities)
        _ <- Task.effectTotal(logger.info(s"retrieved ${activities.size} activities"))
        progress <- Task.fromFuture(_ => storage.listAllActivities(432909, "Ride")).map(DailyProgress.from)
        _ <- Task.effectTotal(printAllProgress(progress))
        _ <- Task.effect(storage.destroy())
      } yield ()
    }
    res.fold(err => {
      logger.error("failed", err)
      ExitCode.failure
    }, _ => ExitCode.success)

  }
}
