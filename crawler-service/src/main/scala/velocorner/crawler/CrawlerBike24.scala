package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.GZip
import org.http4s.{Header, Headers, Method, Uri, UrlForm}
import org.typelevel.ci.CIString
import velocorner.api.Money
import velocorner.api.brand.Marketplace.Bike24
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}

import java.net.URLEncoder

object CrawlerBike24 {

  val baseUrl = Bike24.url.stripSuffix("/")

  // the BC specific request and responses
  case class SuggestImage(path: String, mimeType: String)
  object SuggestImage {
    implicit val codec: Codec[SuggestImage] = deriveCodec
  }

  case class SuggestProduct(
      name: String,
      price: String,
      description: String,
      imageMedium: SuggestImage,
      link: String,
      manufacturer: String,
      reviewStars: Option[Double],
      isNew: Option[Boolean],
      isBuyable: Option[Boolean],
      isOffer: Option[Boolean]
  )
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
        market = Bike24,
        brand = Brand(name = p.manufacturer, logoUrl = none).some,
        name = p.name,
        description = p.description.some,
        price = extractPrice(p.price),
        imageUrl = baseUrl + p.imageMedium.path,
        productUrl = baseUrl + p.link,
        reviewStars = p.reviewStars.getOrElse(0),
        isNew = p.isNew.getOrElse(false),
        onSales = p.isOffer.getOrElse(false),
        onStock = p.isBuyable.getOrElse(true)
      )
    }
  }
  object SuggestResponse {
    implicit val codec: Codec[SuggestResponse] = deriveCodec
  }

  // ---------------- utility functions ----------------
  /**
   * patterns:
   * |54.29€
   * |1,629.08€
   * | <span>from</span> 7.23€
   * | <span>from</span> 5.42€
   */
  def extractPrice(s: String): Money = {
    val amountCcy = s.split('>').last.trim
    PriceParser.parse(amountCcy)
  }
}

class CrawlerBike24[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = Bike24

  /*
   * curl 'https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.14.2)%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.1)%3B%20react%20(17.0.2)%3B%20react-instantsearch%20(6.33.0)' \
    -H 'Accept: */*' \
    -H 'Accept-Language: en-US,en;q=0.9,de;q=0.8,hu;q=0.7,ro;q=0.6' \
    -H 'Cache-Control: no-cache' \
    -H 'Connection: keep-alive' \
    -H 'Origin: https://www.bike24.com' \
    -H 'Pragma: no-cache' \
    -H 'Referer: https://www.bike24.com/' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Site: same-site' \
    -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36' \
    -H 'content-type: application/x-www-form-urlencoded' \
    -H 'sec-ch-ua: "Google Chrome";v="107", "Chromium";v="107", "Not=A?Brand";v="24"' \
    -H 'sec-ch-ua-mobile: ?0' \
    -H 'sec-ch-ua-platform: "macOS"' \
    -H 'x-algolia-api-key: KEY' \
    -H 'x-algolia-application-id: ID' \
    --data-raw '{"requests":[{"indexName":"production_SEARCH_INDEX_EN","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"},{"indexName":"production_BRAND_INDEX","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"}]}' \
    --compressed
   */
  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val term = URLEncoder.encode(searchTerm, "UTF-8")
    val payload = """{"requests":[{"indexName":"production_SEARCH_INDEX_EN","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"},{"indexName":"production_BRAND_INDEX","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"}]}""".stripMargin
    val uri = Uri.unsafeFromString(s"https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.14.2)%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.1)%3B%20react%20(17.0.2)%3B%20react-instantsearch%20(6.33.0)")
    val headers: Headers = Headers(
      Header.Raw(CIString("Accept"), "*/*"),
      Header.Raw(CIString("Accept-Language"), "en-US,en;q=0.9,de;q=0.8"),
      Header.Raw(CIString("Cache-Control"), "no-cache"),
      Header.Raw(CIString("Connection"), "keep-alive"),
      Header.Raw(CIString("Origin"), "https://www.bike24.com"),
      Header.Raw(CIString("Pragma"), "no-cache"),
      Header.Raw(CIString("Referer"), "https://www.bike24.com"),
      Header.Raw(CIString("Sec-Fetch-Dest"), "empty"),
      Header.Raw(CIString("Sec-Fetch-Mode"), "cors"),
      Header.Raw(CIString("Sec-Fetch-Site"), "same-site"),
      Header.Raw(CIString("User-Agent"), "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"),
      Header.Raw(CIString("content-type"), "application/x-www-form-urlencoded"),
      Header.Raw(CIString("sec-ch-ua"), """Google Chrome";v="107", "Chromium";v="107", "Not=A?Brand";v="24""""),
      Header.Raw(CIString("sec-ch-ua-mobile"), "?0"),
      Header.Raw(CIString("sec-ch-ua-platform"), "macOS"),
      Header.Raw(CIString("x-algolia-api-key"), "KEY"),
      Header.Raw(CIString("x-algolia-application-id"), "ID"),
    )
    val form = UrlForm(
      "requests" -> """[{"indexName":"production_SEARCH_INDEX_EN","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"},{"indexName":"production_BRAND_INDEX","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"}]""".stripMargin
    )
    val request = Method.POST(payload, uri, headers)
    //val gzipClient = GZip()(client)
    for {
      res <- client.expect[String](request)
      _ = println(res)
    } yield List.empty[ProductDetails]
  }
}
