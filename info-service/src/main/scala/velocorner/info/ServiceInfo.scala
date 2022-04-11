package velocorner.info

import cats.effect.{IO, IOApp}

import java.util.Calendar

object ServiceInfo extends IOApp.Simple {

  def run: IO[Unit] = for {
    _ <- IO.println(s"ServiceInfo: ${Calendar.getInstance().getTime()}")
  } yield ()

}
