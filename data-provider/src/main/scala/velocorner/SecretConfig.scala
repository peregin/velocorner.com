package velocorner

import com.typesafe.config.{Config, ConfigFactory}
import velocorner.SecretConfig.PimpMyConfig

/** Created by levi on 29/03/15.
  */
object SecretConfig {

  def load(config: Config): SecretConfig = new SecretConfig(config)

  def load(): SecretConfig = load(ConfigFactory.load())

  implicit class PimpMyConfig(config: Config) {

    def getOptAs[T](path: String): Option[T] = {
      val maybePath = if (config.hasPath(path)) Some(path) else None
      maybePath.map(config.getAnyRef).map(_.asInstanceOf[T])
    }
  }

}

case class SecretConfig(config: Config) {

  def isServiceEnabled(application: ServiceProvider.Value): Boolean = config.getOptAs[Boolean](s"$application.enabled").getOrElse(false)

  def getId(application: ServiceProvider.Value): String = config.getString(s"$application.application.id")

  def getToken(application: ServiceProvider.Value): String = config.getString(s"$application.application.token")

  def getSecret(application: ServiceProvider.Value): String = config.getString(s"$application.application.secret")

  def getCallbackUrl(application: ServiceProvider.Value): String = config.getString(s"$application.application.callback.url")

  def getStorageType: Option[String] = config.getOptAs[String]("storage")

  def getOrientDbUrl: Option[String] = config.getOptAs("orientdb.url")

  def getOrientDbPassword: String = config.getString("orientdb.password")

  def getPsqlUrl: String = config.getString("psql.url")

  def getPsqlUser: String = config.getString("psql.user")

  def getPsqlPassword: String = config.getString("psql.password")

  def getProxyHost: Option[String] = config.getOptAs[String]("proxy.host")

  def getProxyPort: Option[Int] = config.getOptAs[Int]("proxy.port")

  def getProxyUser: Option[String] = config.getOptAs[String]("proxy.user")

  def getProxyPassword: Option[String] = config.getOptAs[String]("proxy.password")
}
