package velocorner.manual.brand

import velocorner.feed.BrandFeed

object BrandApp extends App {

  //val entries = BrandFeed.wiggle("wiggle.csv")
  //val entries = BrandFeed.performanceBike("performancebike.csv")
  //val entries = BrandFeed.bikeComponents("bikecomponents.csv")
  val entries = BrandFeed.bikester("bikester.csv")
  entries.foreach(println)
  println(s"${entries.size} entries")
}
