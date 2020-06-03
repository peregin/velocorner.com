package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.MyMacConfig
import velocorner.storage.Storage
import zio.{ExitCode, URIO, ZIO}

object AccountFromStorageApp extends zio.App with LazyLogging with MyMacConfig {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val res = for {
      storage <- ZIO.effect(Storage.create("or"))
      _ <- ZIO.effect(storage.initialize())
      account <- ZIO.fromFuture(_ => storage.getAccountStorage.getAccount(432909))
      _ <- ZIO.effect(logger.info(s"ACCOUNT ${account.toString}"))
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.fold(err => {
        logger.error(s"failed", err)
      ExitCode.failure
      },
      _ => ExitCode.success)
  }
}

