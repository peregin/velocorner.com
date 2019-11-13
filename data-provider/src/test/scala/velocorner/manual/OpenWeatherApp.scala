package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, OpenWeatherFeed}
import zio.ZIO

object OpenWeatherApp extends zio.App with LazyLogging with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      config <- ZIO.apply(SecretConfig.load())
      feed <- ZIO.succeed(new OpenWeatherFeed(config))

      res <- ZIO.fromFuture(_ => feed.forecast("Zurich,CH"))
      _ <- ZIO.effect(logger.info(s"result is $res"))
      _ <- ZIO.effect(logger.info(s"${res.points.size} items"))

      cur <- ZIO.fromFuture(_ => feed.current("Adliswil,CH"))
      _ <- ZIO.effect(logger.info(s"current sunrise/sunset is $cur"))

      _ <- ZIO.apply(feed.close())
      _ <- ZIO.apply(HttpFeed.shutdown())
    } yield ()
    res.fold(_ => 1, _ => 0)
  }
}
