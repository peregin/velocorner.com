package velocorner.storage

import velocorner.model.{AthleteDailyProgress, Account, DailyProgress, Activity}

trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def store(activities: Iterable[Activity])
  def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress]
  def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress]
  def listAllActivityIds(): Iterable[Int]
  // summary on the landing page
  def listRecentActivities(limit: Int): Iterable[Activity]
  // to check how much needs to be imported from the feed
  def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity]
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
