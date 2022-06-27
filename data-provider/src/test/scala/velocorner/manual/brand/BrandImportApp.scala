package velocorner.manual.brand

import velocorner.feed.BrandImport

object BrandImportApp extends App {

  // val entries = BrandFeed.wiggle("wiggle.csv")
  // val entries = BrandFeed.performanceBike("performancebike.csv")
  // val entries = BrandFeed.bikeComponents("bikecomponents.csv")
  // val entries = BrandFeed.bikester("bikester.csv")
  // val entries = BrandFeed.chainReaction("chainreaction.csv")
  val entries = BrandImport.bike24("bike24.csv")
  entries.foreach(println)
  println(s"${entries.size} entries")
}
