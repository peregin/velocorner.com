package velocorner.search.manual.brand

import com.typesafe.scalalogging.LazyLogging
import velocorner.feed.BrandImport
import velocorner.manual.MyLocalConfig
import velocorner.util.JsonIo
import velocorner.api.brand._
import velocorner.api.brand.Marketplace._

import java.io.PrintWriter

object BuildBrandFromCsvManual extends App with LazyLogging with MyLocalConfig {

  // val entries = BrandFeed.wiggle("wiggle.csv")
  // val entries = BrandFeed.performanceBike("performancebike.csv")
  // val entries = BrandFeed.bikeComponents("bikecomponents.csv")
  // val entries = BrandFeed.bikester("bikester.csv")
  // val entries = BrandFeed.chainReaction("chainreaction.csv")
  // val entries = BrandFeed.bike24("bike24.csv")

  val markets = map(BrandImport.wiggle("wiggle.csv"), Wiggle) :::
    map(BrandImport.performanceBike("performancebike.csv"), PerformanceBike) :::
    map(BrandImport.bikeComponents("bikecomponents.csv"), BikeComponents) :::
    map(BrandImport.bikester("bikester.csv"), Bikester) :::
    map(BrandImport.chainReaction("chainreaction.csv"), ChainReactionCycles) :::
    map(BrandImport.bike24("bike24.csv"), Bike24)

  def map(entries: List[BrandUrl], marketplace: Marketplace): List[MarketplaceBrand] = entries.map(e =>
    MarketplaceBrand(
      marketplace = marketplace,
      brand = e.brand,
      url = e.url
    )
  )

  logger.info(s"writing ${markets.size} brands to file")
  val json = JsonIo.write[List[MarketplaceBrand]](markets)
  val pw = new PrintWriter(BrandImport.dir + "/markets.json")
  pw.print(json)
  pw.close()
  logger.info("done...")
}
