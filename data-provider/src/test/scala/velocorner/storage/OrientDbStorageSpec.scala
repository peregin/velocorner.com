package velocorner.storage

import java.io.File

import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.slf4s.Logging
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.manual.AwaitSupport
import velocorner.model.strava.Activity
import velocorner.model.weather.{ForecastResponse, SunriseSunset, WeatherForecast}
import velocorner.util.{FreePortFinder, JsonIo}

class OrientDbStorageSpec extends Specification with BeforeAfterAll with AwaitSupport with Logging {

  sequential
  stopOnFail

  @volatile var storage: OrientDbStorage = _

  "storage" should {

    val zhLocation = "Zurich,CH"

    "check that is empty" in {
      await(storage.dailyProgressForAthlete(432909)) must beEmpty
    }

    "add items as idempotent operation" in {
      val activities = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json").filter(_.`type` == "Ride")
      await(storage.storeActivity(activities))
      await(storage.listRecentActivities(432909, 50)) must haveSize(24)

      // is it idempotent
      await(storage.storeActivity(activities))
      await(storage.listRecentActivities(432909, 50)) must haveSize(24)
    }

    "retrieve recent activities for an athlete" in {
      await(storage.listRecentActivities(432909, 50)) must haveSize(24)
    }

    "retrieve daily stats for an athlete" in {
      await(storage.dailyProgressForAthlete(432909)) must haveSize(15)
    }

    "suggest activities for a specific athlete" in {
      val activities = await(storage.suggestActivities("Stallikon", 432909, 10))
      activities must haveSize(3)
    }

    "suggest no activities when athletes are not specified" in {
      val activities = await(storage.suggestActivities("Stallikon", 1, 10))
      activities must beEmpty
    }

    "suggest activities case insensitive" in {
      val activities = await(storage.suggestActivities("stAlLIkon", 432909, 10))
      activities must haveSize(3)
    }

    "retrieve existing activity" in {
      await(storage.getActivity(244993130)).map(_.id) should beSome(244993130L)
    }

    "return empty on non existent activity" in {
      await(storage.getActivity(111)) must beNone
    }

    "list activity types" in {
      await(storage.listActivityTypes(432909)) should containTheSameElementsAs(Seq("Ride"))
    }

    "select max achievements" in {
      await(storage.getAchievementStorage().maxSpeed()).map(_.value) should beSome(15.5d)
      //await(storage.getAchievementStorage().maxDistance()).map(_.value) should beSome(12d)
    }

    "backup the database" in {
      val file = File.createTempFile("orientdb", "backup")
      storage.backup(file.getAbsolutePath)
      file.length() must beGreaterThan(10L)
      file.delete()
    }

    "read empty list of weather forecast" in {
      val list = await(storage.getWeatherStorage().listRecentForecast(zhLocation))
      list must beEmpty
    }

    "store weather forecast items as idempotent operation" in {
      val weatherStorage = storage.getWeatherStorage()
      val entries = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
      entries must haveSize(40)
      await(weatherStorage.storeWeather(entries.map(e => WeatherForecast(zhLocation, e.dt.getMillis, e))))
      await(weatherStorage.listRecentForecast(zhLocation)) must haveSize(40)
      await(weatherStorage.listRecentForecast("Budapest,HU")) must beEmpty

      // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
      val first = entries.head
      await(weatherStorage.storeWeather(Seq(WeatherForecast(zhLocation, first.dt.getMillis, first))))
      await(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)

      // different location, same timestamp
      await(weatherStorage.storeWeather(Seq(WeatherForecast("Budapest,HU", first.dt.getMillis, first))))
      await(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)
      await(weatherStorage.listRecentForecast("Budapest,HU", limit = 50)) must haveSize(1)
    }

    "store/lookup sunrise/sunset" in {
      val weatherStorage = storage.getWeatherStorage()
      val now = DateTime.now
      val tomorrow = now.plusDays(1)
      await(weatherStorage.getSunriseSunset("bla", "2019")) must beNone
      await(weatherStorage.storeSunriseSunset(SunriseSunset("Budapest", "2019-03-11", now, tomorrow)))
      await(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunrise.toLocalDate) must beSome(now.toLocalDate)
      await(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunset.toLocalDate) must beSome(tomorrow.toLocalDate)
      await(weatherStorage.getSunriseSunset("Zurich", "2019-03-11")) must beNone
      await(weatherStorage.getSunriseSunset("Budapest", "2019-03-12")) must beNone
    }

    "store/lookup attributes" in {
      val attributeStorage = storage.getAttributeStorage()
      await(attributeStorage.getAttribute("key", "test")) must beNone

      await(attributeStorage.storeAttribute("key", "test", "value"))
      await(attributeStorage.getAttribute("key", "test")) must beSome("value")

      await(attributeStorage.getAttribute("key", "test2")) must beNone

      await(attributeStorage.storeAttribute("key", "test", "value2"))
      await(attributeStorage.getAttribute("key", "test")) must beSome("value2")
    }
  }

  override def beforeAll() {
    // eventually the port is already used if the application runs locally
    val serverPort = FreePortFinder.find()
    log.info(s"running OrientDb on port $serverPort")
    storage = new OrientDbStorage("orientdb_data_test", MemoryStorage, serverPort)
    FileUtils.deleteDirectory(new File(storage.rootDir)) // cleanup previous incomplete test remainders
    storage.initialize()
  }

  override def afterAll() {
    storage.destroy()
    FileUtils.deleteDirectory(new File(storage.rootDir))
    storage = null
  }
}
