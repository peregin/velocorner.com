package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.storage.Storage
import zio.Task

object ActivitiesFromStravaAndAggregateApp extends zio.App with LazyLogging with AggregateActivities with MyMacConfig {

  override def run(args: List[String]): zio.URIO[zio.ZEnv, Int] = {
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
        progress <- Task.fromFuture(_ => storage.dailyProgressForAthlete(432909, "Ride"))
        _ <- Task.effectTotal(printAllProgress(progress))
        _ <- Task.effect(storage.destroy())
      } yield ()
    }
    res.fold(err => {
      logger.error("failed", err)
      1
    }, _ => 0)
  }
}
