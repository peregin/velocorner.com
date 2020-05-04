package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres

object EmbeddedPsqlStorage {

  def apply(): EmbeddedPostgres = {
    // without won't work from IntelliJ/Mac/Ubuntu, injects different locale
    val locale = sys.props.get("os.name") match {
      case Some(mac) if mac.toLowerCase.contains("mac") => "en_US"
      case Some(win) if win.toLowerCase.contains("win") => "en_us"
      case _ => "en_US.utf8"
    }
    // postgres can't be executed as root
    EmbeddedPostgres.builder()
      .setLocaleConfig("locale", locale)
      .setLocaleConfig("lc-messages", locale)
      .start()
  }
}
