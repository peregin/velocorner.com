package velocorner.storage

import velocorner.model.{DailyProgress, Progress, Activity}

/**
 * Created by levi on 15/03/15.
 */
trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def store(activities: List[Activity])

  def dailyProgress: List[DailyProgress]
  def overallProgress: List[Progress]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}
