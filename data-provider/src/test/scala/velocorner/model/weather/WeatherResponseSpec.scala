package velocorner.model.weather

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class WeatherResponseSpec extends Specification {

  "Withings response" should {

    val forecast = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/weather.json")

    "be loaded from reference file" in {
      forecast.cod === "200"
      forecast.list must haveSize(40)
    }
  }
}
