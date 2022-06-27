package velocorner.manual.storage

import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport
import zio.{Scope, ZIO, ZIOAppArgs}

object AccountFromStorageApp extends zio.ZIOAppDefault with FlywaySupport with MyLocalConfig {

  def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    for {
      storage <- ZIO.attempt(localPsqlDb)
      _ <- ZIO.succeed(storage.initialize())
      account <- ZIO.fromFuture(_ => storage.getAccountStorage.getAccount(432909))
      _ <- zio.Console.printLine(s"ACCOUNT ${account.toString}")
      _ <- ZIO.attempt(storage.destroy())
    } yield ()
}
