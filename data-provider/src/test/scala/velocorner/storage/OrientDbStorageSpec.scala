package velocorner.storage

import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.api.Activity
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.manual.AwaitSupport
import velocorner.model.DailyProgress
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo
import scala.concurrent.ExecutionContext.Implicits.global

class OrientDbStorageSpec extends Specification with BeforeAfterAll with AwaitSupport with LazyLogging {

  sequential
  stopOnFail

  @volatile var storage: OrientDbStorage = _

  "storage" should {

    val zhLocation = "Zurich,CH"
    val activities = JsonIo
      .readReadFromResource[List[Activity]]("/data/strava/last30activities.json")
      .filter(_.`type` == "Ride")

    "check that is empty" in {
      awaitOn(storage.listAllActivities(432909, "Ride")) must beEmpty
    }

    "add activity twice as upsert" in {
      val single = activities.headOption.toList
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.listAllActivities(432909, "Ride")) must haveSize(1)
    }

    "add items as idempotent operation" in {
      awaitOn(storage.storeActivity(activities))
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(24)

      // is it idempotent
      awaitOn(storage.storeActivity(activities))
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(24)
    }

    "retrieve recent activities for an athlete" in {
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(24)
    }

    "retrieve daily stats for an athlete" in {
      awaitOn(
        storage.listAllActivities(432909, "Ride").map(DailyProgress.from)
      ) must haveSize(15)
      awaitOn(storage.listAllActivities(432909, "Hike")) must beEmpty
    }

    "suggest activities for a specific athlete" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 432909, 10))
      activities must haveSize(3)
    }

    "suggest no activities when athletes are not specified" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 1, 10))
      activities must beEmpty
    }

    "suggest activities case insensitive" in {
      val activities = awaitOn(storage.suggestActivities("stAlLIkon", 432909, 10))
      activities must haveSize(3)
    }

    "retrieve existing activity" in {
      awaitOn(storage.getActivity(244993130)).map(_.id) should beSome(244993130L)
    }

    "return empty on non existent activity" in {
      awaitOn(storage.getActivity(111)) must beNone
    }

    "list activity types" in {
      awaitOn(storage.listActivityTypes(432909)) should containTheSameElementsAs(Seq("Ride"))
    }

    "select achievements" in {
      awaitOn(storage.getAchievementStorage.maxAverageSpeed(432909, "Ride")).map(_.value) should beSome(7.932000160217285d)
      awaitOn(storage.getAchievementStorage.maxDistance(432909, "Ride")).map(_.value) should beSome(90514.3984375d)
      awaitOn(storage.getAchievementStorage.maxElevation(432909, "Ride")).map(_.value) should beSome(1077d)
      awaitOn(storage.getAchievementStorage.maxHeartRate(432909, "Ride")).map(_.value) should beNone
      awaitOn(storage.getAchievementStorage.maxAveragePower(432909, "Ride")).map(_.value) should beSome(233.89999389648438d)
      awaitOn(storage.getAchievementStorage.minAverageTemperature(432909, "Ride")).map(_.value) should beSome(-1d)
      awaitOn(storage.getAchievementStorage.maxAverageTemperature(432909, "Ride")).map(_.value) should beSome(11d)
    }

    "read empty list of weather forecast" in {
      val list = awaitOn(storage.getWeatherStorage.listRecentForecast(zhLocation))
      list must beEmpty
    }

    "store weather forecast items as idempotent operation" in {
      val weatherStorage = storage.getWeatherStorage
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
      val weatherStorage = storage.getWeatherStorage
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
      val attributeStorage = storage.getAttributeStorage
      awaitOn(attributeStorage.getAttribute("key", "test")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value")

      awaitOn(attributeStorage.getAttribute("key", "test2")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value2"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value2")
    }
  }

  override def beforeAll(): Unit = {
    storage = new OrientDbStorage(url = None, dbPassword = "admin")
    storage.initialize()
  }

  override def afterAll(): Unit = {
    storage.destroy()
  }
}
