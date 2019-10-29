package controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.filters.hosts.AllowedHostsConfig
import velocorner.SecretConfig
import velocorner.feed.{OpenWeatherFeed, StravaActivityFeed}
import velocorner.storage.Storage

import scala.concurrent.Future

@Singleton
class ConnectivitySettings @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration) {

  val secretConfig = SecretConfig(configuration.underlying)

  private val logger = Logger.of(this.getClass)
  private val storage = Storage.create("or", secretConfig)
  storage.initialize

  logger.info("ready...")

  def getStorage = storage

  def getStravaFeed = new StravaActivityFeed(None, secretConfig)

  def getStravaFeed(token: String) = new StravaActivityFeed(Some(token), secretConfig)

  def getWeatherFeed = new OpenWeatherFeed(secretConfig)

  def disconnect(): Unit = {
    logger.info("releasing storage connections...")
    getStorage.destroy
    logger.info("stopped...")
  }

  def allowedHosts: Seq[String] = AllowedHostsConfig.fromConfiguration(configuration).allowed

  lifecycle.addStopHook(() => Future.successful(disconnect()))
}
