package velocorner.manual.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.Storage
import zio.ZIO

object AccountFromStorageApp extends zio.App with LazyLogging with AwaitSupport with MyMacConfig {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val res = for {
      storage <- ZIO.succeed(Storage.create("or"))
      _ <- ZIO.apply(storage.initialize())
      account <- ZIO.fromFuture(global => storage.getAccount(432909))
      _ = logger.info(s"ACCOUNT ${account.toString}")
      _ <- ZIO.apply(storage.destroy())
    } yield ()
    res.foldM(err => {
        logger.error(s"failed", err)
        ZIO.succeed(1)
      },
      _ => ZIO.succeed(0))
  }
}

