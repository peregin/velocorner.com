package velocorner.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.model.strava.Activity
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object JsonApp extends App with LazyLogging {

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/club_test.json")
  logger.info(s"found ${list.size} activities")
}
