package velocorner

import com.typesafe.config.{Config, ConfigFactory}

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

  def getBucketPassword = config.getString("couchbase.bucket.password")
}
