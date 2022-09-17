package velocorner.crawler.manual

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.api.Money
import velocorner.crawler.CrawlerBikeComponents

class BikeComponentsTest extends AnyFlatSpec with should.Matchers {

  // 54.29€
  // <span>from</span>  7.23€
  // <span>from</span>  5.42€
  "price extraction" should "detect prices" in {
    CrawlerBikeComponents.extractPrice("54.29€") shouldBe Money(54.29, "EUR")
    CrawlerBikeComponents.extractPrice("88USD") shouldBe Money(88, "USD")
    CrawlerBikeComponents.extractPrice("88 USD") shouldBe Money(88, "USD")
    CrawlerBikeComponents.extractPrice("<span>from</span> 88 USD") shouldBe Money(88, "USD")
    a[IllegalArgumentException] shouldBe thrownBy(CrawlerBikeComponents.extractPrice("€"))
  }
}
