package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.manual.MyLocalConfig
import velocorner.storage.{OrientDbStorage, Storage}
import velocorner.util.JsonIo
import zio.{ExitCode, Task, URIO, ZIO}

import java.io.PrintWriter

object ExportFromOrientDbApp extends zio.App with LazyLogging with MyLocalConfig {

  def writeJson(name: String, activities: Iterable[Activity]): Task[Unit] = {
    ZIO
      .effect(new PrintWriter(name))
      .bracket {
        logger.info(s"file $name has been created ...")
        out => ZIO.effectTotal(out.close())
      } { out =>
        ZIO.effectTotal(out.println(JsonIo.write(activities)))
      }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val athleteId = 432909
    val res = for {
      storage <- ZIO.effect(Storage.create("or"))
      _ <- ZIO.effect(storage.initialize())
      activities <- ZIO.fromFuture(_ => storage.listAllActivities(athleteId, activityType = "Ride"))
      _ = logger.info(s"found ${activities.size} rides ...")
      _ <- writeJson(s"/Users/levi/Downloads/$athleteId.json", activities)
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.fold(
      err => {
        logger.error("failed to extract data", err)
        ExitCode.failure
      },
      _ => ExitCode.success
    )
  }

}
