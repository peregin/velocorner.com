package velocorner.manual

import cats.effect.{IO, IOApp}
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.storage.Storage

object ActivitiesFromStravaToStorageApp extends IOApp.Simple with MyLocalConfig {

  private val config = SecretConfig.load()
  private implicit val feed = new StravaActivityFeed(None, config)

  override def run: IO[Unit] = for {
    storage <- IO(Storage.create("or"))
    _ = storage.initialize()
    // activities <- IO.fromFuture(IO(StravaActivityFeed.listRecentAthleteActivities))
    activities <- IO.fromFuture(IO(StravaActivityFeed.listAllAthleteActivities))
    _ <- IO.println(s"found ${activities.size} activities")
    _ <- IO.fromFuture(IO(storage.storeActivity(activities)))
    _ <- IO.println("done...")
    _ <- IO.blocking {
      storage.destroy()
      feed.close()
      HttpFeed.shutdown()
    }
  } yield ()

}
