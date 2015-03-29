package velocorner

import com.typesafe.config.ConfigFactory

/**
 * Created by levi on 29/03/15.
 */
object SecretConfig {

  lazy val config = ConfigFactory.load()

  def getApplicationToken = config.getString("strava.application.token")

  def getBucketPassword = config.getString("couchbase.bucket.password")
}
