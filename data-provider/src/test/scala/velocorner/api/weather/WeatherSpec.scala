package velocorner.api.weather

import org.joda.time.DateTime
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats.implicits._

class WeatherSpec extends AnyWordSpec with Matchers {

  val forecast = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json")

  "openweathermap.org response" should {

    "be loaded from reference file" in {
      forecast.cod mustBe "200"
      forecast.points must have size 40
      forecast.city.map(_.name) mustBe "Zurich".some
      forecast.city.map(_.country) mustBe "CH".some

      val first = forecast.points.head
      first.dt.compareTo(DateTime.parse("2019-01-19T01:00:00.000+01:00")) mustBe 0
      first.main.temp mustBe -3.71f
      first.main.humidity mustBe 89
      first.weather must have size 1

      val info = first.weather.head
      info.main mustBe "Clear"
    }
  }

  "storage model" should {

    "read and written" in {
      val weather = forecast.points.head
      val storageEntry = WeatherForecast("Zurich, CH", weather.dt, weather)
      val json = JsonIo.write(storageEntry)
      val entity = JsonIo.read[WeatherForecast](json)
      entity === storageEntry
    }
  }

  "list of entries" should {

    "be grouped by day" in {
      val entries = forecast.points.map(w => WeatherForecast("Zurich,CH", w.dt, w))
      val dailyForecast = DailyWeather.list(entries)
      dailyForecast must have size 5
    }
  }
}
