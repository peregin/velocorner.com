package controllers

import play.Logger
import play.api.{Application, GlobalSettings}
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.storage.{CouchbaseStorage, Storage}


object Global extends GlobalSettings {

  @volatile private var storage: Option[Storage] = None
  @volatile private var secretConfig: Option[SecretConfig] = None

  def getFeed = new StravaFeed(None, getSecretConfig)

  def getFeed(token: String) = new StravaFeed(Some(token), getSecretConfig)

  def getStorage = storage.getOrElse(sys.error("storage is not initialized"))

  def getSecretConfig = secretConfig.getOrElse(sys.error("secret configuration is not initialized"))

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    secretConfig = Some(SecretConfig(app.configuration.underlying))
    val appSecret = app.configuration.getString("application.secret")
    Logger.info(s"application secret: ${appSecret.mkString}")

    Logger.info("initializing storage...")
    storage = Some(new CouchbaseStorage(getSecretConfig.getBucketPassword))
    storage.foreach(_.initialize)
    Logger.info("ready...")
  }

  override def onStop(app: Application) {
    Logger.info("releasing storage connections...")
    storage.foreach(_.destroy)
    Logger.info("stopped...")
  }
}
