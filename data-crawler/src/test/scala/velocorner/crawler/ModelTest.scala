package velocorner.crawler

import io.circe.syntax._

import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.api.Money
import velocorner.api.brand.Marketplace.BikeComponents
import velocorner.api.brand.{Brand, ProductDetails}
import model._

class ModelTest extends AnyFlatSpec with should.Matchers {

  "api models" should "be converted by circe to json" in {
    val pd = ProductDetails(
      market = BikeComponents,
      brand = Brand(
        name = "SRAM",
        logoUrl = none
      ).some,
      name = "crank",
      description = "great compinent".some,
      price = Money(10.2, "EUR"),
      imageUrl = "image",
      productUrl = "product/url"
    )
    val json = pd.asJson.spaces4
    json shouldBe
      """{
        |    "market" : {
        |        "name" : "Bike-Components",
        |        "url" : "https://www.bike-components.de/",
        |        "logoUrl" : "https://www.bike-components.de/assets/favicons/android-chrome-192x192.png"
        |    },
        |    "brand" : {
        |        "name" : "SRAM",
        |        "logoUrl" : null
        |    },
        |    "name" : "crank",
        |    "description" : "great compinent",
        |    "price" : {
        |        "value" : 10.2,
        |        "currency" : "EUR"
        |    },
        |    "imageUrl" : "image",
        |    "productUrl" : "product/url"
        |}""".stripMargin
  }
}
