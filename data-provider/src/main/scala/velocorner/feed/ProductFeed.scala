package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.brand.{Marketplace, ProductDetails}
import velocorner.SecretConfig
import velocorner.util.JsonIo

import scala.concurrent.Future

// client interface to the data-crawler service
trait ProductFeed {

  def supported(): Future[List[Marketplace]]

  def search(term: String): Future[List[ProductDetails]]
}

class ProductCrawlerFeed(override val config: SecretConfig) extends HttpFeed with LazyLogging with ProductFeed {

  lazy val baseUrl = config.getCrawlerUrl

  override def supported(): Future[List[Marketplace]] =
    ws(_.url(s"$baseUrl/supported").get())
      .map(_.body)
      .map(JsonIo.read[List[Marketplace]])

  override def search(term: String): Future[List[ProductDetails]] =
    ws(_.url(s"$baseUrl/search/$term").get())
      .map(_.body)
      .map(JsonIo.read[List[ProductDetails]])
}
