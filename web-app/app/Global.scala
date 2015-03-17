import play.Logger
import play.api.{Application, GlobalSettings}


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    val appSecret = app.configuration.getString("application.secret")
    Logger.info(s"application.secret: $appSecret")

    // check main configuration file, must be provided separately with the token setups
    val stravaAppToken = app.configuration.getString("strava.application.token")
    Logger.info(s"strava.application.token: $stravaAppToken")

    //val stats = CouchbaseStorage.logStats()
    //Logger.info(stats.mkString("\n"))
  }

  override def onStop(app: Application) {
    Logger.info("disconnecting from storage...")
    //CouchbaseStorage.disconnect()
  }
}
