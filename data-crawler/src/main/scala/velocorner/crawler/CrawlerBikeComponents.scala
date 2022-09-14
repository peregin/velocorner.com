package velocorner.crawler

import cats.implicits._
import cats.effect.Async
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client._
import org.http4s.{Method, Uri}
import org.http4s.client.dsl.Http4sClientDsl
import velocorner.api.brand.Marketplace.BikeComponents
import velocorner.api.brand.{Marketplace, ProductDetails}
import velocorner.crawler.CrawlerBikeComponents._

object CrawlerBikeComponents {
  case class SuggestProduct(price: String)
  object SuggestProduct {
    implicit val suggestProductCodecodec: Codec[SuggestProduct] = deriveCodec
  }
  case class SuggestResponse(products: List[SuggestProduct]) {
    def toApi(): List[ProductDetails] = List.empty
  }
  object SuggestResponse {
    implicit val suggestResponseCodecodec: Codec[SuggestResponse] = deriveCodec
  }
}

class CrawlerBikeComponents[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = BikeComponents

  override def products(searchTerm: String): F[List[ProductDetails]] = {
    val uri =
      Uri.unsafeFromString("https://www.bike-components.de/en/suggest/term_suggestion/?keywords=sram%20chain%201x10&variant=DEFAULT")
    val request = Method.GET(uri)
    for {
      res <- client.expect[SuggestResponse](request)
    } yield res.toApi()
  }
}
