package velocorner.smile

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.util.{JsonIo, Metrics}
import smile.io._
import smile.regression._

object PredictSmileApp extends App with LazyLogging with Metrics {

  val activities = timed("read compressed activities")(JsonIo.readFromGzipResource[List[Activity]]("/data/432909.json.gz"))
  logger.info(s"loaded ${activities.size} activities")

}
