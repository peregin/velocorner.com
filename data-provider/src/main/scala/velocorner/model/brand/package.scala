package velocorner.model

package object brand {

  case class Brand(name: String, logoUrl: Option[String])

  object Marketplace {
    object Wiggle
        extends Marketplace(
          "Wiggle",
          "https://www.wiggle.co.uk/",
          "https://www.wigglestatic.com/images/ui/wiggle-logo/desktop-wiggle_master_rgb_logo.svg"
        )

    object Bikester
        extends Marketplace(
          "Bikester",
          "https://www.bikester.ch/",
          "https://www.bikester.ch/on/demandware.static/Sites-bikester-ch-Site/-/default/dw3867b4e6/images/logo.svg"
        )

    object Bikecomponents
        extends Marketplace(
          "Bike-Components",
          "https://www.bike-components.de/",
          "https://www.bike-components.de/cache/de46e7f80030462106dd0aa86db679fe.png"
        )

    object Chainreactioncycles
        extends Marketplace(
          "Chain Reaction Cycles",
          "https://www.chainreactioncycles.com/",
          "https://i.pinimg.com/originals/44/2e/07/442e073967b3705bb70a11fed3188833.jpg"
        )

    object Performancebike
        extends Marketplace(
          "Performance Bicycle",
          "https://performancebike.com/",
          "https://www.performancebike.com/cdn-cgi/image/width=600/content/skins/performancebike/images/site-logo.png"
        )

  }

  case class Marketplace(name: String, url: String, logoUrl: String)

  case class BrandUrl(brand: Brand, url: String)

  case class BrandMarketplace(brand: BrandUrl, marketplace: Marketplace)

}
