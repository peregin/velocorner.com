package velocorner.manual.storage

import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.{MigrateOrient2Psql, OrientDbStorage, PsqlDbStorage, Storage}

object MigrateOrient2PsqlApp extends App with AwaitSupport with MyMacConfig {

  val psql = Storage.create("ps").asInstanceOf[PsqlDbStorage]
  val orient = Storage.create("or").asInstanceOf[OrientDbStorage]
  psql.initialize()
  orient.initialize()

  val migration = new MigrateOrient2Psql(orient, psql)
  awaitOn(migration.doIt())

  orient.destroy()
  psql.destroy()
}
