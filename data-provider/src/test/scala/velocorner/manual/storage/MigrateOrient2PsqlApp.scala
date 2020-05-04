package velocorner.manual.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.storage.{MigrateOrient2Psql, OrientDbStorage, PsqlDbStorage, Storage}

object MigrateOrient2PsqlApp extends App with AwaitSupport with MyMacConfig {

  local2local()
  //local2embedded()

  def local2embedded(): Unit = {
    // without won't work from IntelliJ/Mac/Ubuntu, injects different locale
    val locale = sys.props.get("os.name") match {
      case Some(mac) if mac.toLowerCase.contains("mac") => "en_US"
      case Some(win) if win.toLowerCase.contains("win") => "en_us"
      case _ => "en_US.utf8"
    }
    // postgres can't be executed as root
    val embeddedPotgres = EmbeddedPostgres.builder()
      .setLocaleConfig("locale", locale)
      .setLocaleConfig("lc-messages", locale)
      .start()
    val port = embeddedPotgres.getPort
    val postgres = new PsqlDbStorage(dbUrl = s"jdbc:postgresql://localhost:$port/postgres", dbUser = "postgres", dbPassword = "test")
    postgres.initialize()
    val orient = Storage.create("or").asInstanceOf[OrientDbStorage]
    orient.initialize()

    val migration = new MigrateOrient2Psql(orient, postgres)
    awaitOn(migration.doIt())

    orient.destroy()
    postgres.destroy()
    embeddedPotgres.close()
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
