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

  def getApplicationId = config.getString("strava.application.id")
  def getApplicationToken = config.getString("strava.application.token")
  def getApplicationSecret = config.getString("strava.application.secret")
  def getApplicationCallbackUrl = config.getString("strava.application.callback.url")

  def getBucketPassword = config.getString("couchbase.bucket.password")

  def getProxyHost: Option[String] = config.getAs[String]("proxy.host")
  def getProxyPort: Option[Int] = config.getAs[Int]("proxy.port")
  def getProxyUser: Option[String] = config.getAs[String]("proxy.user")
  def getProxyPassword: Option[String] = config.getAs[String]("proxy.password")
}
