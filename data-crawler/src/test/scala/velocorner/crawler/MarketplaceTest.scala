package velocorner.crawler

import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.api.brand.{Brand, ProductDetails}
import velocorner.api.Money
import velocorner.api.brand.Marketplace.VeloFactory

class MarketplaceTest extends AnyFlatSpec with should.Matchers with DecodeResource {

  "bikecomponents api response" should "be converted" in {
    assert[CrawlerBikeComponents.SuggestResponse]("/bikecomponents/suggest.json").toApi().size shouldBe 4
    assert[CrawlerBikeComponents.SuggestResponse]("/bikecomponents/suggest2.json").toApi().size shouldBe 4
  }

  "galaxus api response" should "be converted" in {
    assert[List[CrawlerGalaxus.SearchResponse]]("/galaxus/search.json").flatMap(_.toApi()).size shouldBe 10
  }

  "chainreactioncycles page response" should "be converted" in {
    val page = load("/chainreactioncycles/search.html")
    val products = CrawlerChainReactionCycles.scrape(page, 5)
    products should have size 5
  }

  "bikeimport page response" should "be converted" in {
    val page = load("/bikeimport/search.html")
    val products = CrawlerBikeImport.scrape(page, 5)
    products should have size 5
  }

  "bikester page response" should "be converted" in {
    val page = load("/bikester/search.html")
    val products = CrawlerBikester.scrape(page, 5)
    products should have size 5
  }

  "velofactory api response" should "be converted" in {
    val items = assert[CrawlerVeloFactory.SearchResponse]("/velofactory/search.json").toApi()
    items.size shouldBe 30
    items.head shouldBe ProductDetails(
      market = VeloFactory,
      brand = Brand(name = "SRAM", logoUrl = none).some,
      name = "SRAM XX1 20 11-speed chain",
      description = "The XX1 chain follows a long SRAM tradition of stable, lightweight chains. When every bit of propulsion counts, you can be sure that this chain will deliver the required performance. Rivet type Hollow Pin Lock type Power Lock Number of links 118 Directional Yes, only chain lock Material Steel | Finisch inner link / Finisch outer link: chrome plated / nickel plated Weight 246 g MTB groups X01 XX1 Main group Drive parts Category Mountainbike Product subgroup Chains Activity Cycling Sport E-bike Mountain bike Brand Sram Sales unit Piece NAV Article commission group 820: Sram".some,
      price = Money(46.3, "CHF"),
      imageUrl = "https://www.velofactory.ch/media/image/product/26868/md/sram-xx1-20-11-speed-chain.jpg",
      productUrl = "https://www.velofactory.ch/SRAM-XX1-20-11-speed-chain",
      reviewStars = 0d,
      isNew = false,
      onSales = false,
      onStock = true
    )
  }
}
