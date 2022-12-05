package velocorner.crawler

import cats.effect.{Async, IO}
import cats.implicits._
import fs2.text
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.GZip
import org.http4s.headers.`Content-Type`
import org.http4s.{Header, Headers, HttpVersion, MediaType, Method, Uri, UrlForm}
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
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

  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]

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

  /*
  fetch("https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.14.2)%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.1)%3B%20react%20(17.0.2)%3B%20react-instantsearch%20(6.33.0)", {
    "headers": {
      "accept": "*/*",
      "accept-language": "en-US,en;q=0.9,de;q=0.8,hu;q=0.7,ro;q=0.6",
      "cache-control": "no-cache",
      "content-type": "application/x-www-form-urlencoded",
      "pragma": "no-cache",
      "sec-ch-ua": "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"",
      "sec-ch-ua-mobile": "?0",
      "sec-ch-ua-platform": "\"macOS\"",
      "sec-fetch-dest": "empty",
      "sec-fetch-mode": "cors",
      "sec-fetch-site": "same-site",
      "x-algolia-api-key": "KEY",
      "x-algolia-application-id": "ID"
    },
    "referrer": "https://www.bike24.com/",
    "referrerPolicy": "strict-origin-when-cross-origin",
    "body": "{\"requests\":[{\"indexName\":\"production_SEARCH_INDEX_EN\",\"params\":\"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e\"},{\"indexName\":\"production_BRAND_INDEX\",\"params\":\"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e\"}]}",
    "method": "POST",
    "mode": "cors",
    "credentials": "omit"
  });

  cookieConsent={"algolia":true,"basket-store":true,"emarsys":true,"google-ga":true,"google-optimize":true,"google-tm":true,"trbo":true,"zenloop":true}; _gid=GA1.2.1383392998.1670089453; basketStore={"hash":"come276717d282c59de0a6a87cde7d83dfe638b8af67a86e8.06007517","updated":0}; scarab.visitor="3C5A2CE2464E778"; _ga_SQV0CJSCTV=GS1.1.1670089452.1.1.1670089462.0.0.0; _ga=GA1.2.1107108819.1670089453; __cf_bm=q4dA.UldgtNGDgVTEi37khABx1gdmaSs9KN6yDvOOQo-1670161914-0-AWripEijBU3VY5KPh6GhUmntz583ad3QyR5dmF3W9FXEprgQkoSNvfODKVAGJxNtFqUABO9t3O+bL3QM2pldBpCqlEa/M5+3mwpdlGPFSSly

  fetch("https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.14.2)%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.1)%3B%20react%20(17.0.2)%3B%20react-instantsearch%20(6.33.0)", {
    "headers": {
      "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
      "accept-language": "en-US,en;q=0.9,de;q=0.8,hu;q=0.7,ro;q=0.6",
      "cache-control": "no-cache",
      "pragma": "no-cache",
      "sec-ch-ua": "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"",
      "sec-ch-ua-mobile": "?0",
      "sec-ch-ua-platform": "\"macOS\"",
      "sec-fetch-dest": "document",
      "sec-fetch-mode": "navigate",
      "sec-fetch-site": "none",
      "sec-fetch-user": "?1",
      "upgrade-insecure-requests": "1"
    },
    "referrerPolicy": "strict-origin-when-cross-origin",
    "body": null,
    "method": "GET",
    "mode": "cors",
    "credentials": "include"
  });

  curl \
    --request POST \
    --url 'https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.14.2)%3B%20Browser%20(lite)%3B%20JS%20Helper%20(3.11.1)%3B%20react%20(17.0.2)%3B%20react-instantsearch%20(6.33.0)' \
    --header 'Host: search.bike24.com' \
    --header 'Origin: https://search.bike24.com' \
    --header 'Accept: */*' \
    --header 'Accept-Language: en-US,en;q=0.9,de;q=0.8' \
    --header 'Accept-Encoding: gzip, deflate, br' \
    --header 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36'
   */
  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val term = URLEncoder.encode(searchTerm, "UTF-8")
    val payload =
      """{"requests":[{"indexName":"production_SEARCH_INDEX_EN","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"},{"indexName":"production_BRAND_INDEX","params":"clickAnalytics=true&enablePersonalization=false&facets=%5B%5D&getRankingInfo=true&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&query=SRAM%20xx1&tagFilters=&userToken=41d43441-6a77-4c3e-83d3-2561ee38e36e"}]}""".stripMargin
    val uri = Uri.unsafeFromString("https://search.bike24.com/1/indexes/*/queries?x-algolia-agent=Algolia")
    val headers: Headers = Headers(
      Header.Raw(CIString("Host"), "search.bike24.com"),
      Header.Raw(CIString("Origin"), "https://www.bike24.com"),
      Header.Raw(CIString("Referer"), "https://www.bike24.com"),
      Header.Raw(CIString("Accept"), "*/*"),
      Header.Raw(CIString("Accept-Language"), "en-US,en;q=0.9,de;q=0.8"),
      Header.Raw(CIString("Accept-Encoding"), "gzip, deflate, br"),
      Header.Raw(CIString("Cache-Control"), "no-cache"),
      Header.Raw(CIString("Connection"), "keep-alive"),
      Header.Raw(CIString("Content-Type"), "application/json"),
      Header
        .Raw(CIString("User-Agent"), "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"),
      Header.Raw(CIString("x-algolia-api-key"), "KEY"),
      Header.Raw(CIString("x-algolia-application-id"), "ID")
    )
    val request = Method
      .POST(payload, uri, headers)
      .withEmptyBody
      //.withHttpVersion(HttpVersion.`HTTP/2`) // .withContentType(`Content-Type`(MediaType.application.`x-www-form-urlencoded`))
    println(request.asCurl(_ => false))
    val gzipClient = GZip()(client)
    for {
      res <- gzipClient.run(request).use { rep =>
        for {
          message <- rep.body.through(text.utf8.decode).compile.string
          _ = println(message)
          _ = println(rep.status)
        } yield 42
      }
//      res <- client.expect[String](request).attemptTap{
//        case Left(err) => logger.info(err.toString)
//        case Right(_) => logger.info("got content")
//      }
      _ = println(res)
    } yield List.empty[ProductDetails]
  }
}
