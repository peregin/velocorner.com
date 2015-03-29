package velocorner.manual

import velocorner.model.Activity
import velocorner.util.JsonIo


/**
 * Created by levi on 21/03/15.
 */
object JsonApp extends App {

  val list = JsonIo.readFromFile[List[Activity]]("/Users/levi/Downloads/strava/club_test.json")
  println(s"found ${list.size} activities")
}
