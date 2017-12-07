package velocorner

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

/**
 * Created by levi on 29/03/15.
 */
object SecretConfig {

  def load(config: Config): SecretConfig = new SecretConfig(config)

  def load(): SecretConfig = load(ConfigFactory.load())
}

case class SecretConfig(config: Config) {

  def isWithingsEnabled(): Boolean = config.getAs[Boolean]("withings.enabled").getOrElse(false)
  def getId(application: String) = config.getString(s"$application.application.id")
  def getToken(application: String) = config.getString(s"$application.application.token")
  def getSecret(application: String) = config.getString(s"$application.application.secret")
  def getCallbackUrl(application: String) = config.getString(s"$application.application.callback.url")

  def getBucketPassword = config.getString("couchbase.bucket.password")
  def getOrientDbPath = config.getString("orientdb.path")

  def getProxyHost: Option[String] = config.getAs[String]("proxy.host")
  def getProxyPort: Option[Int] = config.getAs[Int]("proxy.port")
  def getProxyUser: Option[String] = config.getAs[String]("proxy.user")
  def getProxyPassword: Option[String] = config.getAs[String]("proxy.password")
}
