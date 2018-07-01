package controllers

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.Logger
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ConnectivitySettings @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration, actorSystem: ActorSystem) {

  val secretConfig = SecretConfig(configuration.underlying)

  private val storage = Storage.create("or", secretConfig)
  storage.initialize

  configuration.getOptional[String]("storage.backup.directory")foreach{ directory =>
    val frequency = secretConfig.getBackupFrequency
    Logger.info(s"backup at $frequency basis into $directory")
    actorSystem.scheduler.schedule(FiniteDuration(1, "second"), frequency, () => {
      val timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH.mm.ss")
      val now = DateTime.now(DateTimeZone.UTC).toString(timeFormatter)
      val file = s"$directory/$now.zip"
      storage.backup(file)
    })
  }
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
