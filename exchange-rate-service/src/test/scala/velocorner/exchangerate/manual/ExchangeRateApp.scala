package velocorner.exchangerate.manual

import cats.effect.{IO, IOApp}

object ExchangeRateApp extends IOApp.Simple {

  override def run: IO[Unit] = for {
    _ <- IO.println("test")
  } yield ()
}
