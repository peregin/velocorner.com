import play.Logger
import play.api.{Application, GlobalSettings}
import velocorner.storage.Couchbase


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    val appSecret = app.configuration.getString("application.secret").mkString
    Logger.info(s"application.secret: $appSecret")

    // check main configuration file, must be provided separately with the token setups
    val stravaAppToken = app.configuration.getString("strava.application.token").mkString
    Logger.info(s"strava.application.token: $stravaAppToken")

    //val stats = Couchbase.logStats()
    //Logger.info(stats.mkString("\n"))
  }

  override def onStop(app: Application) {
    Logger.info("disconnecting from storage...")
    //Couchbase.disconnect()
  }
}
