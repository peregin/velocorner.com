package velocorner.search.manual

import com.typesafe.scalalogging.LazyLogging
import velocorner.feed.BrandFeed
import velocorner.manual.MyLocalConfig
import velocorner.model.brand.Marketplace._
import velocorner.model.brand.MarketplaceBrand._
import velocorner.model.brand.{BrandUrl, Marketplace, MarketplaceBrand}
import velocorner.util.JsonIo

import java.io.PrintWriter

object BuildBrandFromCsvManual extends App with LazyLogging with MyLocalConfig {

  //val entries = BrandFeed.wiggle("wiggle.csv")
  //val entries = BrandFeed.performanceBike("performancebike.csv")
  //val entries = BrandFeed.bikeComponents("bikecomponents.csv")
  //val entries = BrandFeed.bikester("bikester.csv")
  //val entries = BrandFeed.chainReaction("chainreaction.csv")
  //val entries = BrandFeed.bike24("bike24.csv")

  val markets = map(BrandFeed.wiggle("wiggle.csv"), Wiggle) :::
    map(BrandFeed.performanceBike("performancebike.csv"), PerformanceBike) :::
    map(BrandFeed.bikeComponents("bikecomponents.csv"), BikeComponents) :::
    map(BrandFeed.bikester("bikester.csv"), Bikester) :::
    map(BrandFeed.chainReaction("chainreaction.csv"), ChainReactionCycles) :::
    map(BrandFeed.bike24("bike24.csv"), Bike24)

  def map(entries: List[BrandUrl], marketplace: Marketplace): List[MarketplaceBrand] = entries.map(e =>
    MarketplaceBrand(
      marketplace = marketplace,
      brand = e.brand,
      url = e.url
    )
  )

  logger.info(s"writing ${markets.size} brands to file")
  val json = JsonIo.write[List[MarketplaceBrand]](markets)
  val pw = new PrintWriter(BrandFeed.dir + "/markets.json")
  pw.print(json)
  pw.close()
  logger.info("done...")
}
