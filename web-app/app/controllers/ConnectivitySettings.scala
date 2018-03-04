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

  private val storageType = configuration.getOptional[String]("storage").getOrElse("or")
  Logger.info(s"initializing storage $storageType ...")
  private val storage = Storage.create(storageType, secretConfig)
  storage.initialize
  Logger.info("ready...")

  def getStorage = storage

  def getFeed = new StravaActivityFeed(None, secretConfig)

  def getFeed(token: String) = new StravaActivityFeed(Some(token), secretConfig)

  def disconnect() {
    Logger.info("releasing storage connections...")
    getStorage.destroy
    Logger.info("stopped...")
  }

  lifecycle.addStopHook(() => Future.successful(disconnect()))
}
