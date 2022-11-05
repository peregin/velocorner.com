package velocorner.api

import enumeratum.EnumEntry.LowerCamelcase
import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{Format, Json, Reads, Writes}
import velocorner.brand.NameNormalizer._

//noinspection TypeAnnotation
package object brand {

  object Marketplace extends Enum[Marketplace] with LowerCamelcase {

    object Bike24
        extends Marketplace(
          "Bike24",
          "https://www.bike24.com/",
          "https://assets10.bike24.net/static/images/8e6800bd228af47a3e77.svg"
        )

    // part of crawling
    object BikeComponents
        extends Marketplace(
          "Bike-Components",
          "https://www.bike-components.de/",
          "https://www.bike-components.de/assets/favicons/android-chrome-192x192.png"
        )

    // part of crawling
    object BikeImport
        extends Marketplace(
          "BikeImport",
          "https://bikeimport.ch/",
          "https://bikeimport.ch/images/bikeimport_logo.png"
        )

    // part of crawling
    object Bikester
        extends Marketplace(
          "Bikester",
          "https://www.bikester.ch/",
          "https://www.bikester.ch/on/demandware.static/Sites-bikester-ch-Site/-/default/dw3867b4e6/images/logo.svg"
        )

    // part of crawling
    object ChainReactionCycles
        extends Marketplace(
          "Chain Reaction Cycles",
          "https://www.chainreactioncycles.com/",
          "https://i.pinimg.com/originals/44/2e/07/442e073967b3705bb70a11fed3188833.jpg"
        )

    // part of crawling
    object Galaxus
        extends Marketplace(
          "Galaxus",
          "https://www.galaxus.ch/",
          "https://www.galaxus.ch/static/images/galaxus/logo.svg"
        )

    object PerformanceBike
        extends Marketplace(
          "Performance Bicycle",
          "https://performancebike.com/",
          "https://www.performancebike.com/cdn-cgi/image/width=600/content/skins/performancebike/images/site-logo.png"
        )

    object TopPreise
        extends Marketplace(
          name = "TopPreise",
          url = "https://www.toppreise.ch/",
          logoUrl = "https://www.toppreise.ch/files-cc471683-/images/tp_.png"
        )

    object MountainBikeParts
        extends Marketplace(
          "MountainBike Parts",
          "https://www.mountainbike-parts.ch/",
          "https://www.mountainbike-parts.ch/images/mountain_bike_logo.svg"
        )

    // part of crawling
    object VeloFactory
        extends Marketplace(
          "VeloFactory",
          "https://www.velofactory.ch",
          "https://www.velofactory.ch/bilder/intern/shoplogo/velofactory-logo_v2.jpg"
        )

    object Wiggle
        extends Marketplace(
          "Wiggle",
          "https://www.wiggle.co.uk/",
          "https://www.wigglestatic.com/images/ui/wiggle-logo/desktop-wiggle_master_rgb_logo.svg"
        )

    implicit val marketplaceFormat = Format[Marketplace](Json.reads[Marketplace], Json.writes[Marketplace])

    val values = findValues
  }
  sealed case class Marketplace(name: String, url: String, logoUrl: String) extends EnumEntry {
    def toId: String = name.normalize()
  }

  object Brand {
    implicit val brandFormat = Format[Brand](Json.reads[Brand], Json.writes[Brand])
  }
  case class Brand(name: String, logoUrl: Option[String])

  // it helps extraction on a marketplace site
  case class BrandUrl(brand: Brand, url: String)

  // entry point to search for brand, indexed by name
  object MarketplaceBrand {
    implicit val marketplaceBrandFormat = Format[MarketplaceBrand](Json.reads[MarketplaceBrand], Json.writes[MarketplaceBrand])
    implicit val listFormat = Format[List[MarketplaceBrand]](Reads.list(marketplaceBrandFormat), Writes.list(marketplaceBrandFormat))

    // pairs brands listed on different marketplaces with slightly different names
    def normalize(mb: List[MarketplaceBrand]): List[MarketplaceBrand] = {
      val group = mb.groupBy(_.brand.name.normalize())
      group.flatMap { case (_, list) =>
        val brands = list.map(_.brand)
        val brand = brands.find(_.logoUrl.isDefined).getOrElse(brands.head)
        list.map(_.copy(brand = brand))
      }
    }.toList
  }
  case class MarketplaceBrand(marketplace: Marketplace, brand: Brand, url: String) {
    def toId: String = s"${marketplace.name}/${brand.name}".normalize()
  }
}
