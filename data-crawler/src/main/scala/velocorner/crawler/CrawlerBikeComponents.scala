package velocorner.crawler

import cats.implicits._
import cats.effect.Async
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client._
import org.http4s.{Method, Uri}
import org.http4s.client.dsl.Http4sClientDsl
import velocorner.api.Money
import velocorner.api.brand.Marketplace.BikeComponents
import velocorner.api.brand.{Marketplace, ProductDetails}
import velocorner.crawler.CrawlerBikeComponents._

import java.net.URLEncoder

object CrawlerBikeComponents {

  // the BC specific request and responses
  case class SuggestProduct(name: String, price: String, description: String)
  object SuggestProduct {
    implicit val codec: Codec[SuggestProduct] = deriveCodec
  }

  case class Suggest(products: List[SuggestProduct])
  object Suggest {
    implicit val codec: Codec[Suggest] = deriveCodec
  }

  case class SuggestResponse(term: String, suggestions: Suggest) {
    def toApi(): List[ProductDetails] = suggestions.products.map{ p =>
      ProductDetails(
        market = BikeComponents,
        brand = none, //: Option[Brand],
        name = p.name,
        description = p.description.some,
        price = extractPrice(p.price),
        imageUrl = "todo",
        productUrl = "todo"
      )
    }
  }
  object SuggestResponse {
    implicit val codec: Codec[SuggestResponse] = deriveCodec
  }

  /**
    * patterns:
    * |54.29€
    * | <span>from</span>  7.23€
    * | <span>from</span>  5.42€
    */
  def extractPrice(s: String): Money = {
    val amountCcy = s.trim.split(' ').last
    println(s"price = $amountCcy")
    Money(2, "USD")
  }
}

class CrawlerBikeComponents[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = BikeComponents

  override def products(searchTerm: String): F[List[ProductDetails]] = {
    val term = URLEncoder.encode(searchTerm, "UTF-8")
    val uri = Uri.unsafeFromString(s"https://www.bike-components.de/en/suggest/term_suggestion/?keywords=$term&variant=DEFAULT")
    val request = Method.GET(uri)
    for {
      res <- client.expect[SuggestResponse](request)
    } yield res.toApi()
  }
}
