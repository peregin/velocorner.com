package controllers

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.Logger
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.filters.hosts.AllowedHostsConfig
import velocorner.SecretConfig
import velocorner.feed.{OpenWeatherFeed, StravaActivityFeed}
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ConnectivitySettings @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration, actorSystem: ActorSystem) {

  val secretConfig = SecretConfig(configuration.underlying)

  private val logger = Logger.of(this.getClass)
  private val storage = Storage.create("or", secretConfig)
  storage.initialize

  configuration.getOptional[String]("storage.backup.directory")foreach{ directory =>
    val frequency = secretConfig.getBackupFrequency
    logger.info(s"backup at $frequency basis into $directory")
    actorSystem.scheduler.schedule(FiniteDuration(1, "second"), frequency, new Runnable {
      override def run(): Unit = {
        val timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH.mm.ss")
        val now = DateTime.now(DateTimeZone.UTC).toString(timeFormatter)
        val file = s"$directory/velocorner-$now.zip"
        storage.backup(file)
      }
    })
  }
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
