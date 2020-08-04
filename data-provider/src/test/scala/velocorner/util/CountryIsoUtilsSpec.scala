package velocorner.util

import org.specs2.mutable.Specification

class CountryIsoUtilsSpec extends Specification {

  "weather location" should {

    "read the json list" in {
      val name2Code = CountryIsoUtils.fromResources()
      name2Code must not be empty
      name2Code.get("switzerland") === Some("CH")
      name2Code.get("hungary") === Some("HU")
    }

    "be converted to ISO country code" in {
      CountryIsoUtils.iso("Zurich") === "Zurich"
      CountryIsoUtils.iso("Zurich, Switzerland") === "Zurich,CH"
      CountryIsoUtils.iso("Zurich, Helvetica") === "Zurich, Helvetica"
      CountryIsoUtils.iso("Budapest, Hungary") === "Budapest,HU"
    }

    "normalize list of locations" in {
      val locations = List(
        "adliswil, ch",
        "Adliswil, ch",
        "adliswil",
        "adliswil,CH",
        "Adliswil,CH",
        "Adliswil",
        "Budapest"
      )
      CountryIsoUtils.normalize(locations) should containTheSameElementsAs(List("Adliswil,CH", "Budapest"))
    }
  }
}
