package velocorner.model.weather

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class WeatherResponseSpec extends Specification {

  "Withings response" should {

    val forecast = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/weather.json")

    "be loaded from reference file" in {
      forecast.cod === "200"
      forecast.list must haveSize(40)
      forecast.city.name === "Zurich"
      forecast.city.country === "CH"

      val first = forecast.list.head
      first.dt.compareTo(DateTime.parse("2019-01-19T01:00:00.000+01:00")) === 0
    }
  }
}
