import play.Logger
import play.api.{Application, GlobalSettings}
import velocorner.Controller
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage


object Global extends GlobalSettings {

  @volatile private var dataHandler: Option[Controller] = None

  def getDataHandler = dataHandler.getOrElse(sys.error("data handler is not initialized"))

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    val appSecret = app.configuration.getString("application.secret")
    Logger.info(s"application.secret: ${appSecret.mkString}")

    // check main configuration file, must be provided separately with the token setups
    val stravaAppToken = app.configuration.getString("strava.application.token").getOrElse("strava application token is missing")
    Logger.info(s"strava.application.token: $stravaAppToken")
    val couchbaseBucketPassword = app.configuration.getString("couchbase.bucket.password").getOrElse("couchbase bucket password is missing")

    val feed = new StravaFeed(stravaAppToken)
    val storage = new CouchbaseStorage(couchbaseBucketPassword)
    storage.initialize()
    dataHandler = Some(new Controller(feed, storage))
    Logger.info("ready...")
  }

  override def onStop(app: Application) {
    Logger.info("releasing storage connections...")
    dataHandler.foreach(_.repo.destroy())
    Logger.info("stopped...")
  }
}
