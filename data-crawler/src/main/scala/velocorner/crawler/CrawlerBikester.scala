package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import fs2.text
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.jsoup.Jsoup
import velocorner.api.Money
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}
import velocorner.api.brand.Marketplace.Bikester

import java.net.URLEncoder
import scala.jdk.CollectionConverters._

object CrawlerBikester {

  val baseUrl = Bikester.url.stripSuffix("/")

  def scrape(content: String, limit: Int): List[ProductDetails] = {
    val dom = Jsoup.parse(content)
    val grids = dom.select("div[id=search-result-items] > div").asScala.take(limit)
    grids.map{ g =>
      val box = g.select("div[class = product-tile-inner] > div")
      val productUrl = baseUrl + box.select("a").attr("href")
      val imageUrl = box.select("div[class=product-image] > img").attr("data-src")
      val nameBox = box.select("div[class=cyc-margin_top-2 is-left]")
      val name = nameBox.select("div[class=product-name cyc-typo_body cyc-color-text_secondary]").text()
      val brand = nameBox.select("div[class=cyc-typo_subheader cyc-color-text]").text()
      val price = box.select("div[class=product-pricing cyc-margin_top-1 is-left]").text().trim
      ProductDetails(
        market = Bikester,
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
  // CHF 22.21
  def extractPrice(s: String): Money = {
    val amountCcy = s.split(" ").last + " CHF"
    PriceParser.parse(amountCcy)
  }
}

class CrawlerBikester[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = Bikester

  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val uri = s"https://www.bikester.ch/suche/?q=$search"
    for {
      page <- client.get(uri)(_.body.through(text.utf8.decode).compile.string)
    } yield CrawlerBikester.scrape(page, limit)
  }
}
