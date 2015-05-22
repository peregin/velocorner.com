package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.storage.CouchbaseStorage
import velocorner.util.Metrics

/**
 * Created by levi on 04/04/15.
 */
object ActivitiesFromCouchbaseApp extends App with Logging with Metrics {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val password = SecretConfig.getBucketPassword
  log.info(s"connecting to couchbase bucket with password [$password]...")

  val storage = new CouchbaseStorage(password)
  storage.initialize()

  val list = timed("querying progress") {
    storage.dailyProgress
  }
  log.info(s"${list.take(10).mkString("\n\t", "\n\t", "\n")}")

  val ids = storage.listActivityIds
  log.info(s"got ${ids.size} ids")

  //storage.deleteActivities(ids)
  //log.info("deleted all rides from the storage...")

  storage.destroy()
}
