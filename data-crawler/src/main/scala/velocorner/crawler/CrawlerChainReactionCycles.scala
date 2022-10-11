package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import fs2.text
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.jsoup.Jsoup
import velocorner.api.brand.{Marketplace, ProductDetails}
import velocorner.api.brand.Marketplace.ChainReactionCycles
import velocorner.api.Money

import java.net.URLEncoder
import scala.jdk.CollectionConverters._

object CrawlerChainReactionCycles {

  val baseUrl = ChainReactionCycles.url.stripSuffix("/")

  def scrape(content: String, limit: Int): List[ProductDetails] = {
    val dom = Jsoup.parse(content)
    val grids = dom.select("div[class=products_details_container]").asScala.take(limit)
    grids.map{ g =>
      val desc = g.select("li[class=description] > a")
      val productUrl = baseUrl + desc.attr("href")
      val name = desc.select("h2").text()
      val imageUrl = g.select("div[class=product_image1 placeholder] > a > img").attr("src")
      // has text in range: £419.99&nbsp;-&nbsp;£505.99
      // or in a span element a single price £287.00
      val price = Option(g.select("li[class=fromamt] > span").text().trim).filter(_.nonEmpty)
        .getOrElse(g.select("li[class=fromamt]").text().trim)
        .split("-").headOption.getOrElse("").trim // take the first from the range if any
      ProductDetails(
        market = ChainReactionCycles,
        brand = none,
        name = name,
        description = none,
        price = extractPrice(price),
        imageUrl = imageUrl,
        productUrl = productUrl,
        reviewStars = 0
      )
    }.toList
  }

  // sometimes currency symbols are in front
  def extractPrice(s: String): Money = {
    val amountCcy = if (!s.head.isDigit) s.drop(1) + s.take(1) else s
    PriceParser.parse(amountCcy)
  }
}

class CrawlerChainReactionCycles[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = ChainReactionCycles

  override def products(searchTerm: String): F[List[ProductDetails]] = {
    val limit = 5
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val uri = s"https://www.chainreactioncycles.com/s?q=$search&cat=direct"
    for {
      page <- client.get(uri)(_.body.through(text.utf8.decode).compile.string)
    } yield CrawlerChainReactionCycles.scrape(page, limit)
  }
}
