package velocorner.storage

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import org.specs2.specification.core.Fragments
import velocorner.api.Activity
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo

class OrientDbStorageSpec extends Specification with BeforeAfterAll with ActivityStorageFragments with AccountStorageFragments {

  sequential
  stopOnFail

  @volatile var orientDbStorage: OrientDbStorage = _

  "orientdb storage" should {

    val zhLocation = "Zurich,CH"
    val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

    addFragmentsBlock(activityFragments(orientDbStorage, activityFixtures))

    addFragmentsBlock(accountFragments(orientDbStorage))

    "select achievements" in {
      awaitOn(orientDbStorage.getAchievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) should beSome(7.932000160217285d)
      awaitOn(orientDbStorage.getAchievementStorage.maxDistance(432909, "Ride")).map(_.value) should beSome(90514.3984375d)
      awaitOn(orientDbStorage.getAchievementStorage.maxElevation(432909, "Ride")).map(_.value) should beSome(1077d)
      awaitOn(orientDbStorage.getAchievementStorage.maxHeartRate(432909, "Ride")).map(_.value) should beNone
      awaitOn(orientDbStorage.getAchievementStorage.maxAveragePower(432909, "Ride")).map(_.value) should beSome(233.89999389648438d)
      awaitOn(orientDbStorage.getAchievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) should beSome(-1d)
      awaitOn(orientDbStorage.getAchievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) should beSome(11d)
    }

    "read empty list of weather forecast" in {
      val list = awaitOn(orientDbStorage.getWeatherStorage.listRecentForecast(zhLocation))
      list must beEmpty
    }

    "store weather forecast items as idempotent operation" in {
      val weatherStorage = orientDbStorage.getWeatherStorage
      val entries = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
      entries must haveSize(40)
      awaitOn(weatherStorage.storeWeather(entries.map(e => WeatherForecast(zhLocation, e.dt.getMillis, e))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation)) must haveSize(40)
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU")) must beEmpty

      // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
      val first = entries.head
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast(zhLocation, first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)

      // different location, same timestamp
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast("Budapest,HU", first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU", limit = 50)) must haveSize(1)
    }

    "store/lookup sunrise/sunset" in {
      val weatherStorage = orientDbStorage.getWeatherStorage
      val now = DateTime.now
      val tomorrow = now.plusDays(1)
      awaitOn(weatherStorage.getSunriseSunset("bla", "2019")) must beNone
      awaitOn(weatherStorage.storeSunriseSunset(SunriseSunset("Budapest", "2019-03-11", now, tomorrow)))
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunrise.toLocalDate) must beSome(now.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunset.toLocalDate) must beSome(tomorrow.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Zurich", "2019-03-11")) must beNone
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-12")) must beNone
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
