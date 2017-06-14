package velocorner.manual

import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object JsonApp extends App with Logging {

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/club_test.json")
  log.info(s"found ${list.size} activities")
}
