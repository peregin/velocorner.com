package velocorner.storage

import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.manual.AwaitSupport
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo

import scala.concurrent.Future

trait WeatherStorageBehaviour extends Matchers with AwaitSupport { this: AnyFlatSpec =>

  def weatherFragments(storage: => Storage[Future]): Unit = {

    lazy val weatherStorage = storage.getWeatherStorage
    lazy val fixtures = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
    val zhLocation = "Zurich,CH"

    it should "read empty list of weather forecast" in {
      val list = awaitOn(weatherStorage.listRecentForecast(zhLocation))
      list mustBe empty
    }

    it should "store weather forecast items as idempotent operation" in {
      fixtures must have size 40
      awaitOn(weatherStorage.storeWeather(fixtures.map(e => WeatherForecast(zhLocation, e.dt.getMillis, e))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation)) must have size 40
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU")) mustBe empty

      // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
      val first = fixtures.head
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast(zhLocation, first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must have size 40

      // different location, same timestamp
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast("Budapest,HU", first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must have size 40
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU", limit = 50)) must have size 1
    }

    it should "store/lookup sunrise/sunset" in {
      val now = DateTime.now
      val tomorrow = now.plusDays(1)
      awaitOn(weatherStorage.getSunriseSunset("bla", "2019")) mustBe empty
      awaitOn(weatherStorage.storeSunriseSunset(SunriseSunset("Budapest", "2019-03-11", now, tomorrow)))
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunrise.toLocalDate) mustBe Some(now.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunset.toLocalDate) mustBe Some(tomorrow.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Zurich", "2019-03-11")) mustBe empty
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-12")) mustBe empty
    }
  }
}
