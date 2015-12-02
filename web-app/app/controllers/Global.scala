package controllers

import play.Logger
import play.api.{Application, GlobalSettings}
import velocorner.{SecretConfig, DataHandler}
import velocorner.proxy.StravaFeed
import velocorner.storage.CouchbaseStorage


object Global extends GlobalSettings {

  @volatile private var dataHandler: Option[DataHandler] = None
  @volatile private var secretConfig: Option[SecretConfig] = None

  def getDataHandler = dataHandler.getOrElse(sys.error("data handler is not initialized"))

  def getSecretConfig = secretConfig.getOrElse(sys.error("secret configuration is not initialized"))

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    secretConfig = Some(SecretConfig(app.configuration.underlying))
    val appSecret = app.configuration.getString("application.secret")
    Logger.info(s"app secret: ${appSecret.mkString}")

    val feed = new StravaFeed(None, getSecretConfig)
    val storage = new CouchbaseStorage(getSecretConfig.getBucketPassword)
    storage.initialize()
    dataHandler = Some(new DataHandler(feed, storage))
    Logger.info("ready...")
  }

  override def onStop(app: Application) {
    Logger.info("releasing storage connections...")
    dataHandler.foreach(_.repo.destroy())
    Logger.info("stopped...")
  }
}
