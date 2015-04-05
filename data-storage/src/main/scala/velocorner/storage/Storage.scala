package velocorner.storage

import velocorner.model.{Progress, Activity}

/**
 * Created by levi on 15/03/15.
 */
trait Storage {

  def store(activities: List[Activity])


  // queries the daily or overall progress
  def progress(daily: Boolean): List[Progress]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}
