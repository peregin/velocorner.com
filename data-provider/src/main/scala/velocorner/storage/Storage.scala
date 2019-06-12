package velocorner.storage

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.{SunriseSunset, WeatherForecast}

import scala.concurrent.Future


trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def storeActivity(activities: Iterable[Activity]): Future[Unit]

  // e.g. Ride, Run, etc.
  def listActivityTypes(athleteId: Long): Future[Iterable[String]]

  def dailyProgressForAthlete(athleteId: Long): Future[Iterable[DailyProgress]]

  def getActivity(id: Long): Future[Option[Activity]]

  // to check how much needs to be imported from the feed
  def listRecentActivities(athleteId: Long, limit: Int): Future[Iterable[Activity]]

  // accounts
  def store(account: Account): Future[Unit]
  def getAccount(id: Long): Future[Option[Account]]

  // athletes
  def store(athlete: Athlete): Future[Unit]
  def getAthlete(id: Long): Future[Option[Athlete]]

  // clubs
  def store(club: Club): Future[Unit]
  def getClub(id: Long): Future[Option[Club]]

  def getWeatherStorage(): WeatherStorage
  trait WeatherStorage {
    // weather - location is <city[,countryISO2letter]>
    // limit for 5 day forecast broken down to 3 hours = 8 entries/day and 40 entries/5 days
    def listRecentForecast(location: String, limit: Int = 40): Future[Iterable[WeatherForecast]]
    def storeWeather(forecast: Iterable[WeatherForecast]): Future[Unit]
    def getSunriseSunset(location: String, localDate: String): Future[Option[SunriseSunset]]
    def storeSunriseSunset(sunriseSunset: SunriseSunset): Future[Unit]
  }

  // key value pairs - generic attribute storage
  def getAttributeStorage(): AttributeStorage
  trait AttributeStorage {
    def storeAttribute(key: String, `type`: String, value: String): Future[Unit]
    def getAttribute(key: String, `type`: String): Future[Option[String]]
  }

  // various achievements
  def getAchievementStorage(): AchievementStorage
  trait AchievementStorage {
    def maxSpeed(): Future[Option[Achievement]]
    def maxDistance(): Future[Option[Achievement]]
  }

  // initializes any connections, pools, resources needed to open a storage session
  def initialize()

  // releases any connections, resources used
  def destroy()

  // backup database content into the given file
  def backup(fileName: String)
}

object Storage extends Logging {

  def create(dbType: String): Storage = create(dbType, SecretConfig.load())

  def create(dbType: String, config: SecretConfig): Storage = {
    log.info(s"initializing storage $dbType ...")
    val storage = dbType.toLowerCase match {
      case any if dbType.startsWith("co") => new CouchbaseStorage(config.getBucketPassword)
      case any if dbType.startsWith("re") => new RethinkDbStorage
      case any if dbType.startsWith("mo") => new MongoDbStorage
      case any if dbType.startsWith("or") => new OrientDbStorage(config.getOrientDbPath)
      case unknown => sys.error(s"unknown storage type $unknown")
    }

    log.info(s"connecting to ${storage.getClass.getSimpleName} storage...")
    storage
  }
}
