package velocorner.manual.storage

import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.storage.{EmbeddedPsqlStorage, MigrateOrient2Psql, OrientDbStorage, PsqlDbStorage, Storage}

object MigrateOrient2PsqlApp extends App with AwaitSupport with MyLocalConfig {

  // local2local()
  local2embedded()

  def local2embedded(): Unit = {
    val embeddedPostgres = EmbeddedPsqlStorage()
    val port = embeddedPostgres.getPort
    val postgres = new PsqlDbStorage(dbUrl = s"jdbc:postgresql://localhost:$port/postgres", dbUser = "postgres", dbPassword = "test")
    postgres.initialize()
    val orient = Storage.create("or").asInstanceOf[OrientDbStorage]
    orient.initialize()

    val migration = new MigrateOrient2Psql(orient, postgres)
    awaitOn(migration.doIt())

    orient.destroy()
    postgres.destroy()
    embeddedPostgres.close()
  }

  def local2local(): Unit = {
    val postgres = Storage.create("ps").asInstanceOf[PsqlDbStorage]
    val orient = Storage.create("or").asInstanceOf[OrientDbStorage]
    postgres.initialize()
    orient.initialize()

    val migration = new MigrateOrient2Psql(orient, postgres)
    awaitOn(migration.doIt())

    orient.destroy()
    postgres.destroy()
  }
}
