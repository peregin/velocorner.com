package velocorner.model

import org.specs2.mutable.Specification

class CountryIsoSpec extends Specification {

  "model" should {

    "read the json list" in {
      val name2Code = CountryIso.fromResources()
      name2Code must not be empty
      name2Code.get("switzerland") === Some("CH")
      name2Code.get("hungary") === Some("HU")
    }
  }
}
