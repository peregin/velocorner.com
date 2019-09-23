package velocorner.manual

import org.slf4s.Logging
import scalaz.zio._
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, OpenWeatherFeed}
import velocorner.util.CloseableResource

object OpenWeatherApp extends App with Logging with CloseableResource with MyMacConfig {

  override def run(args: List[String]): ZIO[OpenWeatherApp.Environment, Nothing, Int] = {
    val res = for {
      config <- ZIO.apply(SecretConfig.load())
      feed <- ZIO.succeed(new OpenWeatherFeed(config))

      res <- ZIO.fromFuture(_ => feed.forecast("Zurich,CH"))
      _ = log.info(s"result is $res")
      _ = log.info(s"${res.points.size} items")

      cur <- ZIO.fromFuture(_ => feed.current("Adliswil,CH"))
      _ = log.info(s"current sunrise/sunset is $cur")

      _ <- ZIO.apply(feed.close())
      _ <- ZIO.apply(HttpFeed.shutdown())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}
