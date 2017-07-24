package controllers

import javax.inject.{Inject, Singleton}

import play.Logger
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.storage.Storage

import scala.concurrent.Future

@Singleton
class ConnectivitySettings @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration) {

  val secretConfig = SecretConfig(configuration.underlying)

  val appSecret = configuration.getString("application.secret")
  Logger.info(s"application secret: ${appSecret.mkString}")

  val storageType = configuration.getString("storage").getOrElse("or")
  Logger.info(s"initializing storage $storageType ...")
  val storage = Storage.create(storageType, secretConfig)
  storage.initialize
  Logger.info("ready...")

  def disconnect() {
    Logger.info("releasing storage connections...")
    storage.destroy
    Logger.info("stopped...")
  }

  def getFeed = new StravaActivityFeed(None, secretConfig)

  def getFeed(token: String) = new StravaActivityFeed(Some(token), secretConfig)

  lifecycle.addStopHook(() => Future.successful(disconnect()))
}
