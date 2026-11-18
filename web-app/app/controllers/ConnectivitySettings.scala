package controllers

import play.Logger
import org.apache.pekko.stream.Materializer
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.filters.hosts.AllowedHostsConfig
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.storage.Storage

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ConnectivitySettings @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration, val materializer: Materializer) {

  val secretConfig = SecretConfig(configuration.underlying)

  private val logger = Logger.of(this.getClass)
  private val storage = Storage.create("ps", secretConfig)

  storage.initialize()
  logger.info("ready...")

  def getStorage: Storage[Future] = storage

  def getStravaFeed = new StravaActivityFeed(None, secretConfig, materializer)

  def getStravaFeed(token: String) = new StravaActivityFeed(Some(token), secretConfig, materializer)

  private def disconnect(): Unit = {
    logger.info("releasing storage connections...")
    getStorage.destroy()
    logger.info("stopped...")
  }

  def allowedHosts: Seq[String] = AllowedHostsConfig.fromConfiguration(configuration).allowed

  lifecycle.addStopHook(() => Future.successful(disconnect()))
}
