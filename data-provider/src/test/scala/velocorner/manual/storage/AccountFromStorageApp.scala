package velocorner.manual.storage

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.storage.Storage
import velocorner.util.Metrics


object AccountFromStorageApp extends App with Logging with MyMacConfig {

  val storage = Storage.create("or") // mo
  storage.initialize()

  storage.getAccount(432909) foreach{ a =>
    log.info(a.toString)
  }

  storage.destroy()
}

