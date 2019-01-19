package velocorner.model.weather

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class WeatherSpec extends Specification {

  val forecast = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/weather.json")

  "openweathermap.org response" should {
    "be loaded from reference file" in {
      forecast.cod === "200"
      forecast.list must haveSize(40)
      forecast.city.name === "Zurich"
      forecast.city.country === "CH"

      val first = forecast.list.head
      first.dt.compareTo(DateTime.parse("2019-01-19T01:00:00.000+01:00")) === 0
      first.main.temp === -3.71f
      first.main.humidity === 89
      first.weather must haveSize(1)

      val info = first.weather.head
      info.main === "Clear"
      info.icon === "01n"
    }
  }

  "storage model" should {

    "read and written" in {
      val weather = forecast.list.head
      val storageEntry = WeatherForecast("Zurich, CH", weather)
      val json = JsonIo.write(storageEntry)
      val entity = JsonIo.read[WeatherForecast](json)
      entity === storageEntry
    }
  }
}
