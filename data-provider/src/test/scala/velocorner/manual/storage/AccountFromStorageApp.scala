package velocorner.manual.storage

import org.slf4s.Logging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.Storage


object AccountFromStorageApp extends App with Logging with AwaitSupport with MyMacConfig {

  val storage = Storage.create("or") // mo
  storage.initialize()

  await(storage.getAccount(432909)) foreach{ a =>
    log.info(a.toString)
  }

  storage.destroy()
}

