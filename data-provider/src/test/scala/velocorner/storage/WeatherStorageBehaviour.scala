package velocorner.storage

import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.weather.{CurrentWeather, WeatherForecast}
import velocorner.manual.AwaitSupport
import velocorner.model.weather.{ForecastResponse, WeatherResponse}
import velocorner.util.JsonIo

import scala.concurrent.Future

trait WeatherStorageBehaviour extends Matchers with AwaitSupport { this: AnyFlatSpec =>

  def weatherFragments(storage: => Storage[Future]): Unit = {

    lazy val weatherStorage = storage.getWeatherStorage

    lazy val forecastFixture = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
    lazy val weatherFixture = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/current.json")

    val zhLocation = "Zurich,CH"

    it should "read empty list of weather forecast" in {
      val list = awaitOn(weatherStorage.listRecentForecast(zhLocation))
      list mustBe empty
    }

    it should "store weather forecast items as idempotent operation" in {
      forecastFixture must have size 40
      awaitOn(weatherStorage.storeRecentForecast(forecastFixture.map(e => WeatherForecast(zhLocation, e.dt.getMillis, e))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation)) must have size 40
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU")) mustBe empty

      // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
      val first = forecastFixture.head
      awaitOn(weatherStorage.storeRecentForecast(Seq(WeatherForecast(zhLocation, first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must have size 40

      // different location, same timestamp
      awaitOn(weatherStorage.storeRecentForecast(Seq(WeatherForecast("Budapest,HU", first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must have size 40
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU", limit = 50)) must have size 1
    }

    it should "store/lookup sunrise/sunset" in {
      val now = DateTime.now
      val tomorrow = now.plusDays(1)
      val weather = CurrentWeather(
        location = zhLocation,
        timestamp = weatherFixture.dt.get,
        current = weatherFixture.weather.get.head,
        info = weatherFixture.main.get,
        sunriseSunset = weatherFixture.sys.get
      )
      awaitOn(weatherStorage.getRecentWeather("Budapest")) mustBe empty
      awaitOn(weatherStorage.storeRecentWeather(weather))
      awaitOn(weatherStorage.getRecentWeather(zhLocation)) mustBe Some(weather)
      awaitOn(weatherStorage.getRecentWeather("Zurich")) mustBe empty
      awaitOn(weatherStorage.getRecentWeather("New York")) mustBe empty
    }
  }
}
