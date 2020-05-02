package velocorner.storage

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.api.Activity
import velocorner.util.JsonIo

class OrientDbStorageSpec extends Specification with BeforeAfterAll
  with ActivityStorageFragments with AccountStorageFragments with WeatherStorageFragments {

  sequential
  stopOnFail

  @volatile var orientDbStorage: OrientDbStorage = _

  "orientdb storage" should {

    val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

    addFragmentsBlock(activityFragments(orientDbStorage, activityFixtures))

    addFragmentsBlock(accountFragments(orientDbStorage))

    addFragmentsBlock(weatherFragments(orientDbStorage))

    "select achievements" in {
      awaitOn(orientDbStorage.getAchievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) should beSome(7.932000160217285d)
      awaitOn(orientDbStorage.getAchievementStorage.maxDistance(432909, "Ride")).map(_.value) should beSome(90514.3984375d)
      awaitOn(orientDbStorage.getAchievementStorage.maxElevation(432909, "Ride")).map(_.value) should beSome(1077d)
      awaitOn(orientDbStorage.getAchievementStorage.maxHeartRate(432909, "Ride")).map(_.value) should beNone
      awaitOn(orientDbStorage.getAchievementStorage.maxAveragePower(432909, "Ride")).map(_.value) should beSome(233.89999389648438d)
      awaitOn(orientDbStorage.getAchievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) should beSome(-1d)
      awaitOn(orientDbStorage.getAchievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) should beSome(11d)
    }

    "store/lookup attributes" in {
      val attributeStorage = orientDbStorage.getAttributeStorage
      awaitOn(attributeStorage.getAttribute("key", "test")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value")

      awaitOn(attributeStorage.getAttribute("key", "test2")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value2"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value2")
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
