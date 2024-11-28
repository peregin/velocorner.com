package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.strava.Activity
import velocorner.model.ActionType
import velocorner.model.strava.Gear
import velocorner.util.{FlywaySupport, JsonIo}

class PsqlDbStorageTest
    extends AnyFlatSpec
    with BeforeAndAfterAll
    with Matchers
    with ActivityStorageBehaviour
    with AccountStorageBehaviour
    with FlywaySupport
    with LazyLogging {

  @volatile var psqlEmbedded: EmbeddedPostgres = _
  @volatile var psqlStorage: PsqlDbStorage = _

  private val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

  // will generate fixtures as well
  "activity storage" should behave like activityFragments(psqlStorage, activityFixtures)

  it should "list top 10 activities" in {
    val longestActivities = awaitOn(psqlStorage.listTopActivities(432909, ActionType.Distance, "Ride", 10))
    longestActivities must have size 10
    longestActivities.head.id mustBe 239172388L
    longestActivities.head.distance mustBe 90514.4f
    longestActivities.last.id mustBe 228694981L
    longestActivities.last.distance mustBe 21723.7f

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
    awaitOn(psqlStorage.listActivityYears(432909, "Ride")) must contain theSameElementsAs Seq(2015, 2014)
    awaitOn(psqlStorage.listActivityYears(432909, "Hike")) must contain theSameElementsAs Seq(2015)
    awaitOn(psqlStorage.listActivityYears(432909, "Walk")) mustBe empty
  }

  it should "list activity titles" in {
    awaitOn(psqlStorage.activityTitles(432909, 3)) must contain theSameElementsAs Seq(
      "Stallikon Ride",
      "Morning Ride",
      "Stallikon Ride"
    )
  }

  it should "list last activity" in {
    awaitOn(psqlStorage.getLastActivity(432909)) mustBe Some(activityFixtures.head)
  }

  "account storage" should behave like accountFragments(psqlStorage)

  it should "select achievements" in {
    val achievementStorage = psqlStorage.getAchievementStorage
    awaitOn(achievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) mustBe Some(7.932000160217285d)
    awaitOn(achievementStorage.maxDistance(432909, "Ride")).map(_.value) mustBe Some(90514.3984375d)
    awaitOn(achievementStorage.maxTime(432909, "Ride")).map(_.value) mustBe Some(13886d)
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
    awaitOn(gearStorage.store(gear, Gear.Bike, 1))
    awaitOn(gearStorage.getGear("id20")) mustBe empty
    awaitOn(gearStorage.getGear("id1")) mustBe Some(gear)
    awaitOn(gearStorage.listGears(1)) must contain theSameElementsAs List(gear)
    awaitOn(gearStorage.listGears(999)) mustBe empty
  }

  override def beforeAll(): Unit =
    try {
      val maybeCircleci = sys.env.get("CIRCLECI")
      logger.info(s"maybe circleci: $maybeCircleci")
      val port = if (maybeCircleci.exists(_.toBoolean)) {
        logger.info("connecting to circleci psql...")
        5432
      } else {
        logger.info("starting embedded psql...")
        psqlEmbedded = EmbeddedPsqlStorage()
        psqlEmbedded.getPort
      }
      // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
      // want to have test classes at the end
      psqlStorage = testPsqlDb(port)
      psqlStorage.initialize()
    } catch {
      case any: Exception =>
        logger.error("failed to connect to psql", any)
    }

  override def afterAll(): Unit = {
    psqlStorage.destroy()
    Option(psqlEmbedded).foreach(_.close())
  }
}
