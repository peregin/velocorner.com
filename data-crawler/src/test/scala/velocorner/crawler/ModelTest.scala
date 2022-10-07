package velocorner.crawler

import io.circe.syntax._
import cats.implicits._
import io.circe._
import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import velocorner.api.Money
import velocorner.api.brand.Marketplace.BikeComponents
import velocorner.api.brand.{Brand, ProductDetails}
import model._
import velocorner.crawler.CrawlerBikeComponents.SuggestResponse
import velocorner.util.CloseableResource

import scala.io.Source

class ModelTest extends AnyFlatSpec with should.Matchers with CloseableResource {

  "api models" should "be converted by circe to json" in {
    val pd = ProductDetails(
      market = BikeComponents,
      brand = Brand(
        name = "SRAM",
        logoUrl = none
      ).some,
      name = "crank",
      description = "great component".some,
      price = Money(10.2, "EUR"),
      imageUrl = "image",
      productUrl = "product/url",
      reviewStars = 4
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
        |    "description" : "great component",
        |    "price" : {
        |        "value" : 10.2,
        |        "currency" : "EUR"
        |    },
        |    "imageUrl" : "image",
        |    "productUrl" : "product/url",
        |    "reviewStars" : 4.0
        |}""".stripMargin
  }

  def assert[T: Decoder](resource: String): T = {
    val reply = withCloseable(Source.fromURL(getClass.getResource(resource)))(_.mkString)
    parse(reply) match {
      case Left(f) => fail(f.message)
      case Right(value) =>
        println("parsing succeeded")
        value.as[T] match {
          case Left(err) => fail(err.reason.toString)
          case Right(suggestions) => suggestions
        }
    }
  }

  "api response" should "be converted" in {
    assert[SuggestResponse]("/bikecomponents/suggest.json").suggestions.products.size shouldBe 4
    assert[SuggestResponse]("/bikecomponents/suggest2.json").suggestions.products.size shouldBe 4
  }
}
