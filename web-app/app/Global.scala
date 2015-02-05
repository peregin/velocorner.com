import play.Logger
import play.api.{Application, GlobalSettings}
import storage.Couchbase


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("connecting to storage...")
    val stats = Couchbase.logStats()
    Logger.info(stats.mkString("\n"))
  }

  override def onStop(app: Application) {
    Logger.info("disconnecting from storage...")
    Couchbase.disconnect()
  }
}
