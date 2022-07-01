package velocorner.search

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.model.brand.MarketplaceBrand
import velocorner.model.brand.NameNormalizer._
import velocorner.util.JsonIo

class BrandNormalizerSpec extends AnyWordSpec with Matchers {

  "normalizer" should {
    "identify similar brand names" in {
      val brands = JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz")
      brands.size shouldBe 2233

      val normalized = MarketplaceBrand.normalize(brands)
      val spread = normalized.groupBy(_.brand.name)
      val res = spread.map { case (name, list) => s"$name -> ${list.map(_.marketplace.name).mkString(",")}" }
      res.toList.sorted.foreach(println)
      spread.size shouldBe 1308 // 1309 // 1332 // 1417
    }
  }
}
