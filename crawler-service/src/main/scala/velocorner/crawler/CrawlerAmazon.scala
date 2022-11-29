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
import scala.util.Try

object CrawlerAmazon {

  val baseUrl = Amazon.url.stripSuffix("/")

  def scrape(content: String, limit: Int): List[ProductDetails] = {
    val dom = Jsoup.parse(content)
    val grids = dom
      .select("div[class=s-main-slot s-result-list s-search-results sg-row]")
      .select("div[data-component-type=s-search-result]")
      .asScala
      .take(limit * 2) // not all prices are set
    grids
      .flatMap { g =>
        val nameElement = g.select("div[class=a-section a-spacing-none a-spacing-top-micro s-title-instructions-style] > h2 > a")
        val productUrl = baseUrl + nameElement.attr("href")
        val maybeName = Option(nameElement.select("span").text()).filter(_.trim.nonEmpty)

        val reviewText = g.select("div[class=a-section a-spacing-none a-spacing-top-micro] > div > span").text().takeWhile(_ != ' ')
        val review = Try(java.lang.Double.parseDouble(reviewText)).getOrElse(0d)

        val imageUrl = g.select("img[class=s-image]").attr("src")

        val maybePriceText = g.select("span[class=a-offscreen]").asScala.headOption.map(_.text().trim)
        (maybePriceText, maybeName) match {
          case (Some(priceText), Some(name)) =>
            ProductDetails(
              market = Amazon,
              brand = none,
              name = name,
              description = none,
              price = extractPrice(priceText),
              imageUrl = imageUrl,
              productUrl = productUrl,
              reviewStars = review,
              isNew = false,
              onSales = false,
              onStock = true
            ).some
          case _ => none
        }
      }
      .toList
      .take(limit)
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
