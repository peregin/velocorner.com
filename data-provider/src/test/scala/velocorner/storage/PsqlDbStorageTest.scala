package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.GeoPosition
import velocorner.api.strava.Activity
import velocorner.api.weather.WeatherForecast
import velocorner.model.ActionType
import velocorner.model.strava.Gear
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo

class PsqlDbStorageTest
    extends AnyFlatSpec
    with BeforeAndAfterAll
    with Matchers
    with ActivityStorageBehaviour
    with AccountStorageBehaviour
    with WeatherStorageBehaviour
    with AttributeStorageBehaviour
    with LazyLogging {

  @volatile var psql: EmbeddedPostgres = _
  @volatile var psqlStorage: PsqlDbStorage = _

  val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

  // will generate fixtures as well
  "activity storage" should behave like activityFragments(psqlStorage, activityFixtures)

  it should "list top 10 activities" in {
    val longestActivities = awaitOn(psqlStorage.listTopActivities(432909, ActionType.Distance, "Ride", 10))
    longestActivities must have size 10
    longestActivities.head.id mustBe 244993130L
    longestActivities.head.distance mustBe 23216.8f
    longestActivities.last.id mustBe 240646688L
    longestActivities.last.distance mustBe 7819.9f

    val mostClimbs = awaitOn(psqlStorage.listTopActivities(432909, ActionType.Elevation, "Ride", 10))
    mostClimbs must have size 10
  }

  it should "list all activities between dates" in {
    val date = DateTime.parse("2015-01-23T16:18:17Z").withZone(DateTimeZone.UTC)
    val activities = awaitOn(psqlStorage.listActivities(432909, date.minusDays(1), date))
    activities must have size 2
    val activities2 = awaitOn(psqlStorage.listActivities(111, date.minusDays(1), date))
    activities2 must have size 0
  }

  it should "list ytd activities" in {
    awaitOn(psqlStorage.listYtdActivities(432909, "Ride", 2015)) must have size 12
    awaitOn(psqlStorage.listYtdActivities(432909, "Walk", 2015)) must have size 0
    awaitOn(psqlStorage.listYtdActivities(432909, "Ride", 2014)) must have size 12
  }

  it should "list activity years" in {
    awaitOn(psqlStorage.listActivityYears(432909, "Ride")) must contain theSameElementsAs (Seq(2015, 2014))
    awaitOn(psqlStorage.listActivityYears(432909, "Hike")) must contain theSameElementsAs (Seq(2015))
    awaitOn(psqlStorage.listActivityYears(432909, "Walk")) mustBe empty
  }

  it should "list activity titles" in {
    awaitOn(psqlStorage.activitiesTitles(432909, 3)) must contain theSameElementsAs (Seq(
      "Stallikon Ride",
      "Morning Ride",
      "Stallikon Ride"
    ))
  }

  "account storage" should behave like accountFragments(psqlStorage)

  "weather storage" should behave like weatherFragments(psqlStorage)

  "attribute storage" should behave like attributeFragments(psqlStorage)

  it should "select achievements" in {
    val achievementStorage = psqlStorage.getAchievementStorage
    awaitOn(achievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) mustBe Some(7.932000160217285d)
    awaitOn(achievementStorage.maxDistance(432909, "Ride")).map(_.value) mustBe Some(90514.3984375d)
    awaitOn(achievementStorage.maxElevation(432909, "Ride")).map(_.value) mustBe Some(1077d)
    awaitOn(achievementStorage.maxHeartRate(432909, "Ride")).map(_.value) mustBe empty
    awaitOn(achievementStorage.maxAveragePower(432909, "Ride")).map(_.value) mustBe Some(233.89999389648438d)
    awaitOn(achievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) mustBe Some(-1d)
    awaitOn(achievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) mustBe Some(11d)
  }

  it should "count entries" in {
    val adminStorage = psqlStorage.getAdminStorage
    awaitOn(adminStorage.countAccounts) mustBe 1L
    awaitOn(adminStorage.countActivities) mustBe activityFixtures.size.toLong
    awaitOn(adminStorage.countActiveAccounts) mustBe 1
  }

  it should "store and lookup gears" in {
    lazy val gearStorage = psqlStorage.getGearStorage
    val gear = Gear("id1", "BMC", 12.4f)
    awaitOn(gearStorage.store(gear, Gear.Bike))
    awaitOn(gearStorage.getGear("id20")) mustBe empty
    awaitOn(gearStorage.getGear("id1")) mustBe Some(gear)
  }

  it should "suggest weather locations" in {
    lazy val weatherStorage = psqlStorage.getWeatherStorage
    lazy val fixtures = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
    awaitOn(weatherStorage.storeRecentForecast(fixtures.map(e => WeatherForecast("Budapest,HU", e.dt.getMillis, e))))
    awaitOn(weatherStorage.storeRecentForecast(fixtures.map(e => WeatherForecast("Zurich,CH", e.dt.getMillis, e))))
    awaitOn(weatherStorage.suggestLocations("zur")) must contain theSameElementsAs (List("Zurich,CH"))
    awaitOn(weatherStorage.suggestLocations("bud")) must contain theSameElementsAs (List("Budapest,HU"))
    awaitOn(weatherStorage.suggestLocations("wien")) mustBe empty
  }

  it should "store and lookup geo positions" in {
    lazy val locationStorage = psqlStorage.getLocationStorage
    awaitOn(locationStorage.store("Zurich,CH", GeoPosition(8.52, 47.31)))
    awaitOn(locationStorage.getPosition("Zurich,CH")) mustBe Some(GeoPosition(8.52, 47.31))
    awaitOn(locationStorage.store("Zurich,CH", GeoPosition(8.1, 7.2)))
    awaitOn(locationStorage.getPosition("Zurich,CH")) mustBe Some(GeoPosition(8.1, 7.2))
    awaitOn(locationStorage.getPosition("Budapest,HU")) mustBe empty
  }

  it should "lookup ip to country" in {
    lazy val locationStorage = psqlStorage.getLocationStorage
    awaitOn(locationStorage.getCountry("85.1.45.31")) mustBe Some("CH")
    awaitOn(locationStorage.getCountry("188.156.14.255")) mustBe Some("HU")
  }

  override def beforeAll(): Unit = {
    logger.info("starting embedded psql...")
    try {
      psql = EmbeddedPsqlStorage()
      val port = psql.getPort
      psqlStorage = new PsqlDbStorage(dbUrl = s"jdbc:postgresql://localhost:$port/postgres", dbUser = "postgres", dbPassword = "test")
      psqlStorage.initialize()
    } catch {
      case any: Exception =>
        logger.error("failed to start embedded psql", any)
    }
  }

  override def afterAll(): Unit = {
    psqlStorage.destroy()
    psql.close()
  }
}
