package velocorner.model.brand

import cats.implicits.none
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MarketplaceBrandSpec extends AnyWordSpec with Matchers {

  def brand(marketplaceName: String, brandName: String): MarketplaceBrand = MarketplaceBrand(
    marketplace = Marketplace(name = marketplaceName, url = "", logoUrl = ""),
    brand = Brand(name = brandName, logoUrl = none),
    url = ""
  )

  "model" should {
    "generate id with alphanumeric characters" in {
      brand(Marketplace.BikeComponents.name, "Saddle 12").toId shouldBe "bikecomponents/saddle12"
      brand(Marketplace.ChainReactionCycles.name, "Handle %#bar ;1").toId shouldBe "chainreactioncycles/handlebar1"
    }
  }
}
