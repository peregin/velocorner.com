package velocorner.storage
import velocorner.model._

import com.mongodb.casbah.MongoClient

/**
  * Created by levi on 28/09/16.
  * Access layer to the MongoDb.
  */
class MongoDbStorage extends Storage {

  lazy val mongoClient = MongoClient

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]): Unit = ???

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = ???

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = ???

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = ???

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = ???

  // accounts
  override def store(account: Account): Unit = ???

  override def getAccount(id: Long): Option[Account] = ???

  // athletes
  override def store(athlete: Athlete): Unit = ???

  override def getAthlete(id: Long): Option[Athlete] = ???

  // clubs
  override def store(club: Club): Unit = ???

  override def getClub(id: Long): Option[Club] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    //val coll = db(DB_NAME)
  }

  // releases any connections, resources used
  override def destroy(): Unit = ???
}

object MongoDbStorage {

  val DB_NAME = "velocorner"
}
