package velocorner.search

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.WSAuthScheme
import velocorner.SecretConfig
import velocorner.feed.HttpFeed
import velocorner.model.brand.MarketplaceBrand
import velocorner.util.JsonIo

import scala.concurrent.Future

class BrandSearch(val config: SecretConfig) extends HttpFeed with LazyLogging {

  private val baseUrl = config.getZincUrl
  private val ixName = "marketplace"

  def bulk(brands: List[MarketplaceBrand]): Future[Unit] = {
    logger.debug(s"indexing ${brands.size} brands")
    // Payload - ndjson (newline delimited json) content
    // First line is index action
    // Second line is document data
    val payload = brands
      .map { b =>
        val doc = JsonIo.write(b, pretty = false)
        s"""{ "index":{ "_index":"$ixName" } }
          |$doc""".stripMargin
      }
      .mkString("\n")
    // logger.info(payload)
    val response = ws(_.url(s"$baseUrl/api/_bulk"))
      .withAuth(config.getZincUser, config.getZincPassword, WSAuthScheme.BASIC)
      .post(payload)
    response.map(_.body[String]).map(resp => logger.info(s"resp = $resp"))
  }

  def searchBrands(term: String): Future[List[MarketplaceBrand]] = ???

  def suggestBrands(term: String): Future[List[String]] = ???
}
