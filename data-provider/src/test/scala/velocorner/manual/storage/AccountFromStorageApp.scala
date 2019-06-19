package velocorner.manual.storage

import org.slf4s.Logging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.Storage
import scalaz.zio._


object AccountFromStorageApp extends App with Logging with AwaitSupport with MyMacConfig {

  override def run(args: List[String]): ZIO[AccountFromStorageApp.Environment, Nothing, Int] = {
    val res = for {
      storage <- ZIO.succeed(Storage.create("or"))
      _ <- ZIO.apply(storage.initialize())
      account <- ZIO.fromFuture(global => storage.getAccount(432909))
      _ = log.info(s"ACCOUNT ${account.toString}")
      _ <- ZIO.apply(storage.destroy())
    } yield ()
    res.foldM(_ => ZIO.succeed(1), _ => ZIO.succeed(0))
  }
}

