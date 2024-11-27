package velocorner.util

import cats.implicits.catsSyntaxOptionId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CountryUtilsSpec extends AnyWordSpec with Matchers {

  "country code utils" should {

    "read the country codes from json" in {
      val name2Code = CountryUtils.readCountries()
      name2Code should not be empty
      name2Code.get("switzerland") shouldBe "CH".some
      name2Code.get("hungary") shouldBe "HU".some
    }

    "read the currencies from json" in {
      val code2Currency = CountryUtils.readCurrencies()
      code2Currency.get("CH") shouldBe "CHF".some
      code2Currency.get("HU") shouldBe "HUF".some
    }

    "be converted to ISO country code" in {
      CountryUtils.iso("Zurich") shouldBe "Zurich"
      CountryUtils.iso("Zurich, Switzerland") shouldBe "Zurich,CH"
      CountryUtils.iso("Zurich, Helvetica") shouldBe "Zurich, Helvetica"
      CountryUtils.iso("Budapest, Hungary") shouldBe "Budapest,HU"
      CountryUtils.iso("budapest, hungary") shouldBe "budapest,HU"
      CountryUtils.iso("finale ligure, Italy") shouldBe "finale ligure,IT"
    }

    "beautify lowercase locations" in {
      CountryUtils.beautify("Zurich, CH") shouldBe "Zurich, CH"
      CountryUtils.beautify("adliswil, ch") shouldBe "Adliswil, CH"
      CountryUtils.beautify("adliswil, best") shouldBe "Adliswil, Best"
      CountryUtils.beautify("abu dhabi, ae") shouldBe "Abu Dhabi, AE"
      CountryUtils.beautify("buenos aires, ar") shouldBe "Buenos Aires, AR"
      CountryUtils.beautify("budapest") shouldBe "Budapest"
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
      CountryUtils.normalize(locations) should contain theSameElementsAs List("Adliswil,CH", "Budapest")
    }
  }
}
