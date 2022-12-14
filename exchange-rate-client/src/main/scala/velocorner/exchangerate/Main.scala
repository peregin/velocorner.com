package velocorner.exchangerate

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  override def run: IO[Unit] = for {
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info("hello")
  } yield ()
}
