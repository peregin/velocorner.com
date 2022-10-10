package velocorner.crawler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.crawler.CrawlerBikeComponents.SuggestResponse
import velocorner.crawler.CrawlerGalaxus.SearchResponse

class MarketplaceTest extends AnyFlatSpec with should.Matchers with DecodeResource {

  "bikecomponents api response" should "be converted" in {
    assert[SuggestResponse]("/bikecomponents/suggest.json").toApi().size shouldBe 4
    assert[SuggestResponse]("/bikecomponents/suggest2.json").toApi().size shouldBe 4
  }

  "galaxus api response" should "be converted" in {
    assert[List[SearchResponse]]("/galaxus/search.json").flatMap(_.toApi()).size shouldBe 10
  }

  "chainreactioncycles page response" should "be converted" in {
    val page = load("/chainreactioncycles/search.html")
    val products = CrawlerChainReactionCycles.scrape(page, 5)
    products should have size 5
  }
}
