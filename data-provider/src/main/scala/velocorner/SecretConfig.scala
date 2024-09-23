package velocorner

import com.typesafe.config.{Config, ConfigFactory}
import velocorner.SecretConfig.PimpMyConfig

/**
 * Created by levi on 29/03/15.
 */
object SecretConfig {

  def load(config: Config): SecretConfig = new SecretConfig(config)

  //def load(): SecretConfig = load(ConfigFactory.systemEnvironment())
  def load(): SecretConfig = load(ConfigFactory.load())

  implicit class PimpMyConfig(config: Config) {

    def getOptAs[T](path: String): Option[T] = {
      val maybePath = Some(path).filter(config.hasPath)
      maybePath.map(config.getAnyRef).map(_.asInstanceOf[T])
    }
  }

}

case class SecretConfig(config: Config) {

  def isServiceEnabled(application: ServiceProvider.Value): Boolean = config.getOptAs[Boolean](s"$application.enabled").getOrElse(false)

  def getAuthId(application: ServiceProvider.Value): String = config.getString(s"$application.application.id")

  def getAuthToken(application: ServiceProvider.Value): String = config.getString(s"$application.application.token")

  def getAuthSecret(application: ServiceProvider.Value): String = config.getString(s"$application.application.secret")

  def getAuthCallbackUrl(application: ServiceProvider.Value): String = config.getString(s"$application.application.callback.url")

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

  def getJwtSecret: String = config.getString("jwt.secret")

  def getElasticSearchUrl: String = config.getString("elasticsearch.url")
  def getZincUrl: String = config.getString("zinc.url")
  def getZincUser: String = config.getString("zinc.user")
  def getZincPassword: String = config.getString("zinc.password")

  def getCrawlerUrl: String = config.getString("crawler.url")
  def getRatesUrl: String = config.getString("rates.url")
  def getWeatherUrl: String = config.getString("weather.url")
}
