package velocorner.storage

import velocorner.model._

trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def store(activities: Iterable[Activity])
  def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress]
  def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress]
  // summary on the landing page
  def listRecentActivities(limit: Int): Iterable[Activity]
  // to check how much needs to be imported from the feed
  def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity]

  // accounts
  def store(account: Account)
  def getAccount(id: Long): Option[Account]

  // athletes
  def store(athlete: Athlete)
  def getAthlete(id: Long): Option[Athlete]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}
