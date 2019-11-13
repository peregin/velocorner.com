package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.MyMacConfig
import velocorner.storage.Storage
import zio.ZIO

object AccountFromStorageApp extends zio.App with LazyLogging with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      storage <- ZIO.effect(Storage.create("or"))
      _ <- ZIO.effect(storage.initialize())
      account <- ZIO.fromFuture(_ => storage.getAccount(432909))
      _ <- ZIO.effect(logger.info(s"ACCOUNT ${account.toString}"))
      _ <- ZIO.effect(storage.destroy())
    } yield ()
    res.foldM(err => {
        logger.error(s"failed", err)
        ZIO.succeed(1)
      },
      _ => ZIO.succeed(0))
  }
}

