package velocorner.storage

import org.slf4s.Logging
import velocorner.SecretConfig
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

  // clubs
  def store(club: Club)
  def getClub(id: Long): Option[Club]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()
}

object Storage extends Logging {

  def create(dbType: String): Storage = dbType.toLowerCase match {
    case any if dbType.startsWith("co") =>
      val password = SecretConfig.load().getBucketPassword
      log.info(s"connecting to couchbase bucket...")
      new CouchbaseStorage(password)

    case any if dbType.startsWith("re") =>
      log.info(s"connecting to rethink server...")
      new RethinkDbStorage

    case unknown =>
      sys.error(s"unknown storage type $dbType")
  }
}
