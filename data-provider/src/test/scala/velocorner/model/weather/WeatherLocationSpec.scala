package velocorner.model.weather

import org.specs2.mutable.Specification

class WeatherLocationSpec extends Specification {

  "weather location" should {
    "be converted to ISO country code" in {
      WeatherLocation.iso("Zurich") === "Zurich"
      WeatherLocation.iso("Zurich, Switzerland") === "Zurich,CH"
      WeatherLocation.iso("Zurich, Helvetica") === "Zurich, Helvetica"
      WeatherLocation.iso("Budapest, Hungary") === "Budapest,HU"
    }
  }
}
