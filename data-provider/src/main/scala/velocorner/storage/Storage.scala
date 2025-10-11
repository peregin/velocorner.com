package velocorner.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.model._
import velocorner.model.strava.Gear
import velocorner.api.{Account, Achievement}

import scala.concurrent.Future
import org.joda.time.DateTime
import velocorner.api.strava.Activity

trait AccountStorage[M[_]] {
  def store(account: Account): M[Unit]
  def getAccount(id: Long): M[Option[Account]]
}

trait GearStorage[M[_]] {
  def store(gear: Gear, `type`: Gear.Entry, athleteId: Long): M[Unit]
  def getGear(id: String): M[Option[Gear]]
  def listGears(athleteId: Long): M[Iterable[Gear]]
}

trait AchievementStorage[M[_]] {
  def maxAverageSpeed(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxDistance(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxTime(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxElevation(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxHeartRate(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxAverageHeartRate(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxAveragePower(athleteId: Long, activity: String): M[Option[Achievement]]
  def minAverageTemperature(athleteId: Long, activity: String): M[Option[Achievement]]
  def maxAverageTemperature(athleteId: Long, activity: String): M[Option[Achievement]]
}

trait AdminStorage[M[_]] {
  def countAccounts: M[Long]
  def countActivities: M[Long]
  def countActiveAccounts: M[Long]
}

trait Storage[M[_]] {

  // insert all activities, new ones are added, previous ones are overridden
  def storeActivity(activities: Iterable[Activity]): M[Unit]
  // e.g. Ride, Run, etc.
  def listActivityTypes(athleteId: Long): M[Iterable[String]]
  // e.g. 2021, 2020, 2018, etc.
  def listActivityYears(athleteId: Long, activityType: String): M[Iterable[Int]]
  // to create the ytd and aggregated statistics
  def listAllActivities(athleteId: Long, activityType: String): M[Iterable[Activity]]
  def listYtdActivities(athleteId: Long, activityType: String, year: Int): M[Iterable[Activity]]
  def listActivities(athleteId: Long, from: DateTime, to: DateTime): M[Iterable[Activity]]
  // to check how much needs to be imported from the feed
  def listRecentActivities(athleteId: Long, limit: Int): M[Iterable[Activity]]
  def listTopActivities(athleteId: Long, actionType: ActionType.Entry, activityType: String, limit: Int): M[Iterable[Activity]]
  def getLastActivity(athleteId: Long): M[Option[Activity]]
  def getActivity(id: Long): M[Option[Activity]]

  // search and analytics
  def suggestActivities(snippet: String, athleteId: Long, max: Int): M[Iterable[Activity]]
  def activityTitles(athleteId: Long, max: Int): M[Iterable[String]]

  // accounts
  def getAccountStorage: AccountStorage[M]

  // gears
  def getGearStorage: GearStorage[M]

  // various achievements
  def getAchievementStorage: AchievementStorage[M]

  def getAdminStorage: AdminStorage[M]

  // initializes any connections, pools, resources needed to open a storage session
  def initialize(): Unit

  // releases any connections, resources used
  def destroy(): Unit
}

object Storage extends LazyLogging {

  def create(dbType: String): Storage[Future] = create(dbType, SecretConfig.load())

  def create(dbType: String, config: SecretConfig): Storage[Future] = {
    logger.info(s"initializing storage $dbType ...")

    val storage = dbType.toLowerCase match {
      // case any if any.startsWith("re") => new RethinkDbStorage[Future]
      // case any if any.startsWith("mo") => new MongoDbStorage
      // case any if any.startsWith("or") => new OrientDbStorage(config.getOrientDbUrl, config.getOrientDbPassword)
      case any if any.startsWith("ps") => new PsqlDbStorage(config.getPsqlUrl, config.getPsqlUser, config.getPsqlPassword)
      case unknown                     => sys.error(s"unknown storage type $unknown")
    }

    logger.info(s"connecting to ${storage.getClass.getSimpleName} storage...")
    storage
  }
}
