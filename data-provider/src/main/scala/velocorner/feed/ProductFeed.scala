package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.brand.Marketplace
import velocorner.SecretConfig
import velocorner.util.JsonIo

import scala.concurrent.Future

// client interface to the data-crawler service
trait ProductFeed {

  def supported(): Future[List[Marketplace]]
}

class ProductCrawlerFeed(override val config: SecretConfig) extends HttpFeed with LazyLogging with ProductFeed {

  lazy val baseUrl = config.getCrawlerUrl

  override def supported(): Future[List[Marketplace]] =
    ws(_.url(s"$baseUrl/supported").get())
      .map(_.body)
      .map(JsonIo.read[List[Marketplace]])

}
