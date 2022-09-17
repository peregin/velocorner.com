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
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}
import velocorner.crawler.CrawlerBikeComponents._

import java.net.URLEncoder

object CrawlerBikeComponents {

  // the BC specific request and responses
  case class SuggestImage(path: String, mimeType: String)
  object SuggestImage {
    implicit val codec: Codec[SuggestImage] = deriveCodec
  }

  case class SuggestProduct(name: String, price: String, description: String, imageMedium: SuggestImage, link: String, manufacturer: String)
  object SuggestProduct {
    implicit val codec: Codec[SuggestProduct] = deriveCodec
  }

  case class SuggestManufacturer(name: String, image: String)
  object SuggestManufacturer {
    implicit val codec: Codec[SuggestManufacturer] = deriveCodec
  }

  case class Suggest(products: List[SuggestProduct], manufacturers: List[SuggestManufacturer])
  object Suggest {
    implicit val codec: Codec[Suggest] = deriveCodec
  }

  case class SuggestResponse(term: String, suggestions: Suggest) {
    def toApi(): List[ProductDetails] = suggestions.products.map { p =>
      ProductDetails(
        market = BikeComponents,
        brand = Brand(name = p.manufacturer, logoUrl = none).some,
        name = p.name,
        description = p.description.some,
        price = extractPrice(p.price),
        imageUrl = p.imageMedium.path,
        productUrl = p.link
      )
    }
  }
  object SuggestResponse {
    implicit val codec: Codec[SuggestResponse] = deriveCodec
  }

  // ---------------- utility functions ----------------

  val pricePattern = "([\\d\\s.,]*\\d)\\s*(\\S*)".r

  /**
   * patterns:
   * |54.29€
   * | <span>from</span> 7.23€
   * | <span>from</span> 5.42€
   */
  def extractPrice(s: String): Money = {
    val amountCcy = s.split('>').last.trim
    amountCcy match {
      case pricePattern(amount, currency) => Money(BigDecimal(amount), normalizeCurrency(currency))
      case other                          => throw new IllegalArgumentException(s"invalid price pattern $other")
    }
  }

  def normalizeCurrency(c: String): String = c match {
    case "€"   => "EUR"
    case "$"   => "USD"
    case other => other
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
