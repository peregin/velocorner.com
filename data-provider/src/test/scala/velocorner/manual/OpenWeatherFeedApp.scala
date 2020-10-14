package velocorner.manual

import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, OpenWeatherFeed}
import zio.logging._
import zio.{ExitCode, URIO, ZIO}

object OpenWeatherFeedApp extends zio.App with MyLocalConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = for {
      config <- ZIO.effect(SecretConfig.load())
      _ <- ZIO.effect(new OpenWeatherFeed(config)).bracket{ feed =>
        ZIO.effectTotal{
          feed.close()
          HttpFeed.shutdown()
        }
      }{feed =>
        for {
          forecast <- ZIO.fromFuture(_ => feed.forecast("Zurich,CH"))
          _ <- log.info(s"result is $forecast")
          _ <- log.info(s"${forecast.points.size} items")
          sunset <- ZIO.fromFuture(_ => feed.current("Adliswil,CH"))
          _ <- log.info(s"current sunrise/sunset is $sunset")
        } yield ()
      }
    } yield ()
    res.fold(_ => ExitCode.failure, _ => ExitCode.success).provideLayer(zEnv)
  }
}
