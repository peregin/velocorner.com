package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{Header, Headers, Method, Uri}
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString
import velocorner.api.Money
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}
import velocorner.api.brand.Marketplace.VeloFactory
import velocorner.crawler.CrawlerVeloFactory.SearchResponse

import java.net.URLEncoder

object CrawlerVeloFactory {

  case class VeloFactoryProduct(
      title: String,
      brand: String,
      description: Option[String],
      best_price: Option[Double],
      price: Double,
      image_link: String,
      link: String
  )

  object VeloFactoryProduct {
    implicit val codec: Codec[VeloFactoryProduct] = deriveCodec
  }

  case class SearchResponse(results: List[VeloFactoryProduct]) {
    def toApi(): List[ProductDetails] = results.map { p =>
      ProductDetails(
        market = VeloFactory,
        brand = Brand(name = p.brand, logoUrl = none).some,
        name = p.title,
        description = p.description,
        price = Money(BigDecimal(p.best_price.getOrElse(p.price)), "CHF"),
        imageUrl = p.image_link,
        productUrl = p.link,
        reviewStars = 0
      )
    }
  }

  object SearchResponse {
    implicit val codec: Codec[SearchResponse] = deriveCodec
  }
}

class CrawlerVeloFactory[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = VeloFactory

  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val headers: Headers = Headers(
      Header.Raw(CIString("authority"), "eu1-search.doofinder.com"),
      Header.Raw(CIString("user-agent"), "Mozilla/5.0"),
      Header.Raw(CIString("accept"), "*/*"),
      Header.Raw(CIString("origin"), "https://www.velofactory.ch"),
      Header.Raw(CIString("referer"), "https://www.velofactory.ch"),
      Header.Raw(
        CIString("path"),
        s"/5/search?hashid=54e8e92f3b6055a6b454a8b88d75f76d&query_counter=5&page=1&rpp=30&transformer=&session_id=cf831bcf7ad2d7021bdbb28f61c6fbf0&query=$search"
      )
    )
    val uri =
      s"https://eu1-search.doofinder.com/5/search?hashid=54e8e92f3b6055a6b454a8b88d75f76d&query_counter=6&page=1&rpp=30&transformer=&session_id=cf831bcf7ad2d7021bdbb28f61c6fbf0&query=$search"
    val req = Method.GET(Uri.unsafeFromString(uri), headers)
    for {
      res <- client.expect[SearchResponse](req)
    } yield res.toApi().take(limit)
  }
}
