package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import fs2.text
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.jsoup.Jsoup
import velocorner.api.Money
import velocorner.api.brand.Marketplace.BikeImport
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}

import java.net.URLEncoder
import scala.jdk.CollectionConverters._

object CrawlerBikeImport {

  val baseUrl = BikeImport.url.stripSuffix("/")

  def scrape(content: String, limit: Int): List[ProductDetails] = {
    val dom = Jsoup.parse(content)
    val grids = dom.select("div[class=categoryProduct]").asScala.take(limit)
    grids.map{ g =>
      val productUrl = baseUrl + g.select("a").attr("href")
      val imageUrl = baseUrl + g.select("a > img").attr("src")
      val title = g.select("div[class=title]")
      val brand = title.select("span[class=brand]").text()
      val name = title.text()
      val price = g.select("div[class=price]").text().trim
      ProductDetails(
        market = BikeImport,
        brand = Brand(brand, none).some,
        name = name,
        description = none,
        price = extractPrice(price),
        imageUrl = imageUrl,
        productUrl = productUrl,
        reviewStars = 0
      )
    }.toList
  }

  // CHF 12.1
  // ab CHF 22.21
  def extractPrice(s: String): Money = {
    val amountCcy = s.split(" ").last + " CHF"
    PriceParser.parse(amountCcy)
  }
}

class CrawlerBikeImport[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = BikeImport

  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val uri = s"https://bikeimport.ch/shop/search/$search"
    for {
      page <- client.get(uri)(_.body.through(text.utf8.decode).compile.string)
    } yield CrawlerBikeImport.scrape(page, limit)
  }
}
