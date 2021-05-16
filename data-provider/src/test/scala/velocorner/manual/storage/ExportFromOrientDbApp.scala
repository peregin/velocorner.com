package velocorner.manual.storage

import java.io.PrintWriter

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.manual.MyLocalConfig
import velocorner.storage.{OrientDbStorage, Storage}
import velocorner.util.JsonIo
import zio.{ExitCode, Task, URIO, ZIO}

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
      activities <- ZIO.fromFuture(_ => storage.asInstanceOf[OrientDbStorage].listActivities(athleteId, activityType = None))
      _ = logger.info(s"found ${activities.size} activities ...")
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
