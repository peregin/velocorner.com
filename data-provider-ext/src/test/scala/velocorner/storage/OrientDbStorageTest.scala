package velocorner.storage

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.strava.Activity
import velocorner.util.JsonIo

@org.scalatest.Ignore
class OrientDbStorageTest extends AnyFlatSpec with BeforeAndAfterAll with Matchers with ActivityStorageBehaviour with AccountStorageBehaviour {

  @volatile var orientDbStorage: OrientDbStorage = _

  private val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

  "activity storage" should behave like activityFragments(orientDbStorage, activityFixtures)

  "account storage" should behave like accountFragments(orientDbStorage)

  ignore should "select achievements" in {
    val achievementStorage = orientDbStorage.getAchievementStorage
    awaitOn(achievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) mustBe Some(7.932000160217285d)
    awaitOn(achievementStorage.maxDistance(432909, "Ride")).map(_.value) mustBe Some(90514.3984375d)
    awaitOn(achievementStorage.maxElevation(432909, "Ride")).map(_.value) mustBe Some(1077d)
    awaitOn(achievementStorage.maxHeartRate(432909, "Ride")).map(_.value) mustBe empty
    awaitOn(achievementStorage.maxAveragePower(432909, "Ride")).map(_.value) mustBe Some(233.89999389648438d)
    awaitOn(achievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) mustBe Some(-1d)
    awaitOn(achievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) mustBe Some(11d)
  }

  override def beforeAll(): Unit = {
    orientDbStorage = new OrientDbStorage(url = None, dbPassword = "admin")
    orientDbStorage.initialize()
  }

  override def afterAll(): Unit =
    orientDbStorage.destroy()
}
