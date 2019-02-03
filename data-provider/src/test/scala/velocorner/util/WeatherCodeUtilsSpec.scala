package velocorner.util

import org.specs2.mutable.Specification
import velocorner.model.weather.WeatherCode

class WeatherCodeUtilsSpec extends Specification {

  "weather code mappings" should {
    "be converted to model" in {
      val map = WeatherCodeUtils.fromResources()
      map should have size 54
      map.get(200) === Some(WeatherCode(200, "thunderstorm with light rain", "icon-weather-005"))
      map.get(804) === Some(WeatherCode(804, "overcast clouds", "icon-weather-022"))
    }
  }
}
