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

    "read the capitals from json" in {
      val code2Capital = CountryUtils.readCapitals()
      code2Capital.get("CH") shouldBe "Berne".some
      code2Capital.get("HU") shouldBe "Budapest".some
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
