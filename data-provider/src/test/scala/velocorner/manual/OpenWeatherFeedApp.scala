package velocorner.manual

import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, OpenWeatherFeed}

object OpenWeatherFeedApp extends IOApp.Simple with LazyLogging with MyLocalConfig {

  override def run: IO[Unit] = for {
    config <- IO.delay(SecretConfig.load())
    _ <- IO.bracketFull(_ => IO.delay(new OpenWeatherFeed(config)))(feed =>
      for {
        forecast <- IO.fromFuture(IO(feed.forecast("Zurich,CH")))
        _ <- IO.println(s"current forecast is $forecast")
        _ <- IO.println(s"${forecast.points.size} items")
        weather <- IO.fromFuture(IO(feed.current("Adliswil,CH")))
        _ <- IO.println(s"current weather is $weather")
      } yield ()
    )((feed, _) =>
      IO.delay {
        feed.close()
        HttpFeed.shutdown()
      }
    )
  } yield ()
}
