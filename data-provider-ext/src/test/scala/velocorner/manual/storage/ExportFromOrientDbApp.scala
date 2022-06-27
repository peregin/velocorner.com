package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.manual.MyLocalConfig
import velocorner.storage.Storage
import velocorner.util.JsonIo
import zio.{Scope, Task, ZIO, ZIOAppArgs}

import java.io.PrintWriter

object ExportFromOrientDbApp extends zio.ZIOAppDefault with LazyLogging with MyLocalConfig {

  def writeJson(name: String, activities: Iterable[Activity]): Task[Unit] =
    ZIO
      .acquireReleaseWith(ZIO.attempt(new PrintWriter(name))) { out =>
        logger.info(s"file $name has been created ...")
        ZIO.succeed(out.close())
      } { out =>
        ZIO.attempt(out.println(JsonIo.write(activities)))
      }

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] = {
    val athleteId = 432909
    for {
      storage <- ZIO.attempt(Storage.create("or"))
      _ <- ZIO.attempt(storage.initialize())
      activities <- ZIO.fromFuture(_ => storage.listAllActivities(athleteId, activityType = "Ride"))
      _ = logger.info(s"found ${activities.size} rides ...")
      _ <- writeJson(s"/Users/levi/Downloads/$athleteId.json", activities)
      _ <- ZIO.attempt(storage.destroy())
    } yield ()
  }

}
