package velocorner.storage

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.api.Activity
import velocorner.util.JsonIo

class OrientDbStorageSpec extends Specification with BeforeAfterAll
  with ActivityStorageFragments with AccountStorageFragments with WeatherStorageFragments with AttributeStorageFragments {

  sequential
  stopOnFail

  @volatile var orientDbStorage: OrientDbStorage = _

  "orientdb storage" should {

    val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

    addFragmentsBlock(activityFragments(orientDbStorage, activityFixtures))

    addFragmentsBlock(accountFragments(orientDbStorage))

    addFragmentsBlock(weatherFragments(orientDbStorage))

    addFragmentsBlock(attributeFragments(orientDbStorage))

    "select achievements" in {
      val achievementStorage = orientDbStorage.getAchievementStorage
      awaitOn(achievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) should beSome(7.932000160217285d)
      awaitOn(achievementStorage.maxDistance(432909, "Ride")).map(_.value) should beSome(90514.3984375d)
      awaitOn(achievementStorage.maxElevation(432909, "Ride")).map(_.value) should beSome(1077d)
      awaitOn(achievementStorage.maxHeartRate(432909, "Ride")).map(_.value) should beNone
      awaitOn(achievementStorage.maxAveragePower(432909, "Ride")).map(_.value) should beSome(233.89999389648438d)
      awaitOn(achievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) should beSome(-1d)
      awaitOn(achievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) should beSome(11d)
    }
  }

  override def beforeAll(): Unit = {
    orientDbStorage = new OrientDbStorage(url = None, dbPassword = "admin")
    orientDbStorage.initialize()
  }

  override def afterAll(): Unit = {
    orientDbStorage.destroy()
  }
}
