package velocorner.storage

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.manual.AwaitSupport
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo

import scala.concurrent.Future

trait WeatherStorageFragments extends Specification with AwaitSupport {

  def weatherFragments(storage: => Storage[Future]): Fragment = {

    lazy val weatherStorage = storage.getWeatherStorage
    lazy val fixtures = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json").points
    val zhLocation = "Zurich,CH"

    "read empty list of weather forecast" in {
      val list = awaitOn(weatherStorage.listRecentForecast(zhLocation))
      list must beEmpty
    }

    "store weather forecast items as idempotent operation" in {
      fixtures must haveSize(40)
      awaitOn(weatherStorage.storeWeather(fixtures.map(e => WeatherForecast(zhLocation, e.dt.getMillis, e))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation)) must haveSize(40)
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU")) must beEmpty

      // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
      val first = fixtures.head
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast(zhLocation, first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)

      // different location, same timestamp
      awaitOn(weatherStorage.storeWeather(Seq(WeatherForecast("Budapest,HU", first.dt.getMillis, first))))
      awaitOn(weatherStorage.listRecentForecast(zhLocation, limit = 50)) must haveSize(40)
      awaitOn(weatherStorage.listRecentForecast("Budapest,HU", limit = 50)) must haveSize(1)
    }

    "store/lookup sunrise/sunset" in {
      val now = DateTime.now
      val tomorrow = now.plusDays(1)
      awaitOn(weatherStorage.getSunriseSunset("bla", "2019")) must beNone
      awaitOn(weatherStorage.storeSunriseSunset(SunriseSunset("Budapest", "2019-03-11", now, tomorrow)))
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunrise.toLocalDate) must beSome(now.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-11")).map(_.sunset.toLocalDate) must beSome(tomorrow.toLocalDate)
      awaitOn(weatherStorage.getSunriseSunset("Zurich", "2019-03-11")) must beNone
      awaitOn(weatherStorage.getSunriseSunset("Budapest", "2019-03-12")) must beNone
    }
  }
}
