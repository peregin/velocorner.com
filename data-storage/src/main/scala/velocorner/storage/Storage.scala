package velocorner.storage

import velocorner.model.{Account, DailyProgress, Activity}

trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def store(activities: Iterable[Activity])
  def dailyProgress(athleteId: Int): Iterable[DailyProgress]
  def listAllActivityIds(): Iterable[Int]
  def listRecentActivities(athleteId: Option[Int], limit: Int): Iterable[Activity]
  def deleteActivities(ids: Iterable[Int])

  // accounts
  def store(account: Account)
  def getAccount(id: Long): Option[Account]
  def listAllAccountIds(): Iterable[String]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}
