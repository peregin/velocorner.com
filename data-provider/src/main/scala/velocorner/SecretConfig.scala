package velocorner

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import SecretConfig.PimpMyConfig

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * Created by levi on 29/03/15.
 */
object SecretConfig {

  def load(config: Config): SecretConfig = new SecretConfig(config)

  def load(): SecretConfig = load(ConfigFactory.load())


  implicit class PimpMyConfig(config: Config) {

    def getAs[T](path: String): Option[T] = {
      val maybePath = if (config.hasPath(path)) Some(path) else None
      maybePath.map(config.getAnyRef).map(_.asInstanceOf[T])
    }
  }
}

case class SecretConfig(config: Config) {

  def isWithingsEnabled(): Boolean = config.getAs[Boolean]("withings.enabled").getOrElse(false)
  def getId(application: String) = config.getString(s"$application.application.id")
  def getToken(application: String) = config.getString(s"$application.application.token")
  def getSecret(application: String) = config.getString(s"$application.application.secret")
  def getCallbackUrl(application: String) = config.getString(s"$application.application.callback.url")

  def getStorageType: Option[String] = config.getAs[String]("storage")
  def getBucketPassword = config.getString("couchbase.bucket.password")
  def getOrientDbPath = config.getString("orientdb.path")

  def getBackupDirectory: Option[String] = config.getAs[String]("storage.backup.directory")
  def getBackupFrequency: FiniteDuration = {
    val duration = Duration(config.getString("storage.backup.frequency"))
    FiniteDuration(duration.toMillis, TimeUnit.MILLISECONDS)
  }

  def getProxyHost: Option[String] = config.getAs[String]("proxy.host")
  def getProxyPort: Option[Int] = config.getAs[Int]("proxy.port")
  def getProxyUser: Option[String] = config.getAs[String]("proxy.user")
  def getProxyPassword: Option[String] = config.getAs[String]("proxy.password")
}
