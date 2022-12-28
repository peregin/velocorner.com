package velocorner.manual.storage

import cats.effect.{IO, IOApp}
import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport

object AccountFromStorageApp extends IOApp.Simple with FlywaySupport with MyLocalConfig {

  override def run: IO[Unit] = for {
    storage <- IO(localPsqlDb)
    _ <- IO.delay(storage.initialize())
    account <- IO.fromFuture(IO(storage.getAccountStorage.getAccount(432909)))
    _ <- IO.println(s"ACCOUNT ${account.toString}")
    _ <- IO.delay(storage.destroy())
  } yield ()
}
