package velocorner.manual.storage

import velocorner.manual.MyLocalConfig
import velocorner.util.FlywaySupport
import zio.{ExitCode, URIO, ZIO}
import zio.logging._

object AccountFromStorageApp extends zio.App with FlywaySupport with MyLocalConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = for {
      storage <- ZIO.effect(localPsqlDb)
      _ <- ZIO.effect(storage.initialize())
      account <- ZIO.fromFuture(_ => storage.getAccountStorage.getAccount(432909))
      _ <- log.info(s"ACCOUNT ${account.toString}")
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.provideLayer(zEnv).exitCode
  }
}
