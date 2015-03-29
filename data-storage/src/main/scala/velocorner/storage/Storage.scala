package velocorner.storage

import velocorner.model.Activity

/**
 * Created by levi on 15/03/15.
 */
trait Storage {

  def storeClub(activities: List[Activity])

  def storeAthlete(activities: List[Activity])

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}
