package velocorner.smile

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.strava.Activity
import velocorner.util.{JsonIo, Metrics}

object PredictSmileApp extends App with LazyLogging with Metrics {

  val activities = timed("read compressed activities")(JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz"))
  logger.info(s"loaded ${activities.size} activities")
}
