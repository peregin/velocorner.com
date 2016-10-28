package controllers

import play.Logger
import play.api.mvc.{Result, RequestHeader}
import play.api.{Application, GlobalSettings}
import play.api.mvc.Results._
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.storage.{CouchbaseStorage, Storage}

import scala.concurrent.Future


object Global extends GlobalSettings {

  @volatile private var storage: Option[Storage] = None
  @volatile private var secretConfig: Option[SecretConfig] = None

  def getFeed = new StravaActivityFeed(None, getSecretConfig)

  def getFeed(token: String) = new StravaActivityFeed(Some(token), getSecretConfig)

  def getStorage = storage.getOrElse(sys.error("storage is not initialized"))

  def getSecretConfig = secretConfig.getOrElse(sys.error("secret configuration is not initialized"))

  override def onStart(app: Application) {
    Logger.info("starting the application...")

    // check included configuration file
    secretConfig = Some(SecretConfig(app.configuration.underlying))
    val appSecret = app.configuration.getString("application.secret")
    Logger.info(s"application secret: ${appSecret.mkString}")

    val storageType = app.configuration.getString("storage").getOrElse("mo")
    Logger.info(s"initializing storage $storageType ...")
    storage = Some(Storage.create(storageType, getSecretConfig))
    storage.foreach(_.initialize)
    Logger.info("ready...")
  }

  override def onStop(app: Application) {
    Logger.info("releasing storage connections...")
    storage.foreach(_.destroy)
    Logger.info("stopped...")
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Future.successful((NotFound(views.html.notFound())))
  }
}
