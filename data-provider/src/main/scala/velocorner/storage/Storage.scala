package velocorner.storage

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model._
import velocorner.model.strava.{Activity, Athlete, Club}
import velocorner.model.weather.WeatherForecast


trait Storage {

  // insert all activities, new ones are added, previous ones are overridden
  def storeActivity(activities: Iterable[Activity])

  def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress]
  def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress]

  def getActivity(id: Int): Option[Activity]
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

  // weather - location is <city[,countryISO2letter]>
  // limit for 5 day forecast broken down to 3 hours = 8 entries/day and 40 entries/5 days
  def listRecentForecast(location: String, limit: Int = 40): Iterable[WeatherForecast]
  def storeWeather(forecast: Iterable[WeatherForecast])

  // key value pairs - generic attribute storage
  def storeAttribute(key: String, `type`: String, value: String)
  def getAttribute(key: String, `type`: String): Option[String]

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
