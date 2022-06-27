package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, OpenWeatherFeed}
import zio.{Scope, ZIO, ZIOAppArgs}

object OpenWeatherFeedApp extends zio.ZIOAppDefault with LazyLogging with MyLocalConfig {

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    for {
      config <- ZIO.succeed(SecretConfig.load())
      _ <- ZIO.acquireReleaseWith(ZIO.attempt(new OpenWeatherFeed(config))) { feed =>
        ZIO.succeed {
          feed.close()
          HttpFeed.shutdown()
        }
      } { feed =>
        for {
          forecast <- ZIO.fromFuture(_ => feed.forecast("Zurich,CH"))
          _ <- zio.Console.printLine(s"current forecast is $forecast")
          _ <- zio.Console.printLine(s"${forecast.points.size} items")
          weather <- ZIO.fromFuture(_ => feed.current("Adliswil,CH"))
          _ <- zio.Console.printLine(s"current weather is $weather")
        } yield ()
      }
    } yield ()
}
