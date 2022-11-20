package velocorner.crawler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.api.Money

class PriceParserTest extends AnyFlatSpec with should.Matchers {

  "price parser" should "extract" in {
    PriceParser.parse("54.29€") shouldBe Money(54.29, "EUR")
    PriceParser.parse("1,629.08€") shouldBe Money(1629.08, "EUR")
    PriceParser.parse("88USD") shouldBe Money(88, "USD")
    PriceParser.parse("88 USD") shouldBe Money(88, "USD")
    PriceParser.parse("1'010.70 CHF") shouldBe Money(1010.7, "CHF")

    a[IllegalArgumentException] shouldBe thrownBy(CrawlerBikeComponents.extractPrice("€"))
  }

  // 54.29€
  // <span>from</span>  7.23€
  // <span>from</span>  5.42€
  "bike components" should "detect prices" in {
    CrawlerBikeComponents.extractPrice("54.29€") shouldBe Money(54.29, "EUR")
    CrawlerBikeComponents.extractPrice("<span>from</span> 88 USD") shouldBe Money(88, "USD")
  }

  "chain reaction cycles" should "detect prices" in {
    CrawlerChainReactionCycles.extractPrice("£287.00") shouldBe Money(287, "GBP")
    CrawlerChainReactionCycles.extractPrice("$1013.49") shouldBe Money(1013.49, "USD")
    CrawlerChainReactionCycles.extractPrice("$1013.49") shouldBe Money(1013.49, "USD")
    CrawlerChainReactionCycles.extractPrice("€10") shouldBe Money(10, "EUR")
    CrawlerChainReactionCycles.extractPrice("12 USD") shouldBe Money(12, "USD")
    CrawlerChainReactionCycles.extractPrice("32 CHF") shouldBe Money(32, "CHF")
  }
}
