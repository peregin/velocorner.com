package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import fs2.text
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.GZip
import org.jsoup.Jsoup
import velocorner.api.Money
import velocorner.api.brand.Marketplace.Amazon
import velocorner.api.brand.{Marketplace, ProductDetails}

import java.net.URLEncoder
import scala.jdk.CollectionConverters._

object CrawlerAmazon {

  val baseUrl = Amazon.url.stripSuffix("/")

  def scrape(content: String, limit: Int): List[ProductDetails] = {
    val dom = Jsoup.parse(content)
    val grids = dom.select("div[class=s-main-slot s-result-list s-search-results sg-row]").asScala.take(limit)
    println(grids)
    grids.map { g =>
      val aaa = g.attr("data-component-type")
      println(s"aaaa=$aaa")
      val desc = g.select("li[class=description] > a")
      val productUrl = baseUrl + desc.attr("href")
      val name = desc.select("h2").text()
      val imageUrl = g.select("div[class=product_image1 placeholder] > a > img").attr("src")
      // has text in range: £419.99&nbsp;-&nbsp;£505.99
      // or in a span element a single price £287.00
      val price = Option(g.select("li[class=fromamt] > span").text().trim)
        .filter(_.nonEmpty)
        .getOrElse(g.select("li[class=fromamt]").text().trim)
        .split("-")
        .headOption
        .getOrElse("")
        .trim // take the first from the range if any
      ProductDetails(
        market = Amazon,
        brand = none,
        name = name,
        description = none,
        price = Money(10, "USD"), // extractPrice(price),
        imageUrl = imageUrl,
        productUrl = productUrl,
        reviewStars = 0,
        isNew = false,
        onSales = false,
        onStock = true
      )
    }.toList
  }

  // sometimes currency symbols are in front
  def extractPrice(s: String): Money = {
    val amountCcy = if (!s.head.isDigit) s.drop(1) + s.take(1) else s
    PriceParser.parse(amountCcy)
  }
}

class CrawlerAmazon[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = Amazon

  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val link = s"https://www.amazon.com/s?k=$search&i=sporting-intl-ship&ref=nb_sb_noss"
    val limit = 5
    val gzClient = GZip()(client)
    for {
      page <- gzClient.get(Uri.unsafeFromString(link))(_.body.through(text.utf8.decode).compile.string)
    } yield CrawlerAmazon.scrape(page, limit)
  }
}
