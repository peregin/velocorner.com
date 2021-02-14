package velocorner.storage

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.model._
import velocorner.model.strava.{Athlete, Club, Gear}
import velocorner.api.{Achievement, GeoPosition}
import velocorner.api.weather.{SunriseSunset, WeatherForecast}

import scala.concurrent.Future
import cats.instances.future._
import org.joda.time.DateTime
import velocorner.api.strava.Activity

import scala.concurrent.ExecutionContext.Implicits.global

trait Storage[M[_]] {

  // insert all activities, new ones are added, previous ones are overridden
  def storeActivity(activities: Iterable[Activity]): M[Unit]
  // e.g. Ride, Run, etc.
  def listActivityTypes(athleteId: Long): M[Iterable[String]]
  // e.g. 2021, 2020, 2018, etc.
  def listActivityYears(athleteId: Long): M[Iterable[Int]]
  // to create the ytd and aggregated statistics
  def listAllActivities(athleteId: Long, activityType: String): M[Iterable[Activity]]
  def listActivities(athleteId: Long, from: DateTime, to: DateTime): M[Iterable[Activity]]
  // to check how much needs to be imported from the feed
  def listRecentActivities(athleteId: Long, limit: Int): M[Iterable[Activity]]
  def getActivity(id: Long): M[Option[Activity]]
  def suggestActivities(snippet: String, athleteId: Long, max: Int): M[Iterable[Activity]]

  // accounts
  def getAccountStorage: AccountStorage
  trait AccountStorage {
    def store(account: Account): M[Unit]
    def getAccount(id: Long): M[Option[Account]]
  }

  // gears
  def getGearStorage: GearStorage
  trait GearStorage {
    def store(gear: Gear, `type`: Gear.Entry): M[Unit]
    def getGear(id: String): M[Option[Gear]]
  }

  def getWeatherStorage: WeatherStorage
  trait WeatherStorage {
    // weather - location is <city[,countryISO2letter]>
    // limit for 5 day forecast broken down to 3 hours = 8 entries/day and 40 entries/5 days
    def listRecentForecast(location: String, limit: Int = 40): M[Iterable[WeatherForecast]]
    def storeWeather(forecast: Iterable[WeatherForecast]): M[Unit]
    def getSunriseSunset(location: String, localDate: String): M[Option[SunriseSunset]]
    def storeSunriseSunset(sunriseSunset: SunriseSunset): M[Unit]
    def suggestLocations(snippet: String): M[Iterable[String]]
  }

  // key value pairs - generic attribute storage
  def getAttributeStorage: AttributeStorage
  trait AttributeStorage {
    def storeAttribute(key: String, `type`: String, value: String): M[Unit]
    def getAttribute(key: String, `type`: String): M[Option[String]]
  }

  // various achievements
  def getAchievementStorage: AchievementStorage
  trait AchievementStorage {
    def maxAverageSpeed(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxDistance(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxElevation(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxHeartRate(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxAverageHeartRate(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxAveragePower(athleteId: Long, activity: String): M[Option[Achievement]]
    def minAverageTemperature(athleteId: Long, activity: String): M[Option[Achievement]]
    def maxAverageTemperature(athleteId: Long, activity: String): M[Option[Achievement]]
  }

  def getLocationStorage: LocationStorage
  trait LocationStorage {
    def store(location: String, position: GeoPosition): M[Unit]
    def getPosition(location: String): M[Option[GeoPosition]]
    def getCountry(ip: String): M[Option[String]]
  }

  def getAdminStorage: AdminStorage
  trait AdminStorage {
    def countAccounts: M[Long]
    def countActivities: M[Long]
    def countActiveAccounts: M[Long]
  }

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
      case any if any.startsWith("re") => new RethinkDbStorage[Future]
      case any if any.startsWith("mo") => new MongoDbStorage
      case any if any.startsWith("or") => new OrientDbStorage(config.getOrientDbUrl, config.getOrientDbPassword)
      case any if any.startsWith("ps") => new PsqlDbStorage(config.getPsqlUrl, config.getPsqlUser, config.getPsqlPassword)
      case unknown                     => sys.error(s"unknown storage type $unknown")
    }

    logger.info(s"connecting to ${storage.getClass.getSimpleName} storage...")
    storage
  }
}
