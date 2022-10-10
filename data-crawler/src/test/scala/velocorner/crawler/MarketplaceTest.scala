package velocorner.crawler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.crawler.CrawlerBikeComponents.SuggestResponse
import velocorner.crawler.CrawlerGalaxus.{Data, SearchResponse}

class MarketplaceTest extends AnyFlatSpec with should.Matchers with DecodeResource {

  "bikecomponents api response" should "be converted" in {
    assert[SuggestResponse]("/bikecomponents/suggest.json").toApi().size shouldBe 4
    assert[SuggestResponse]("/bikecomponents/suggest2.json").toApi().size shouldBe 4
  }

  "galaxus api response" should "be converted" in {
    assert[List[SearchResponse]]("/galaxus/search.json").flatMap(_.toApi()).size shouldBe 10
  }
}
