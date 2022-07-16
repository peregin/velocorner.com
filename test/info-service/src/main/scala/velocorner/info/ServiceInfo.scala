package velocorner.info

import cats.effect.{IO, IOApp}

import java.util.Calendar

object ServiceInfo extends IOApp.Simple {

  def run: IO[Unit] = IO.println(s"ServiceInfo: ${Calendar.getInstance().getTime()}")

}
