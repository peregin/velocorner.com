package velocorner.search

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.WSAuthScheme
import velocorner.SecretConfig
import velocorner.feed.HttpFeed
import velocorner.model.brand.MarketplaceBrand
import velocorner.util.JsonIo

import scala.concurrent.Future

class BrandSearch(val config: SecretConfig) extends HttpFeed with LazyLogging {

  object SearchMeta {
    implicit val format = Format[SearchMeta](Json.reads[SearchMeta], Json.writes[SearchMeta])
  }
  case class SearchMeta(_id: String, _source: MarketplaceBrand)
  object Hits {
    implicit val format = Format[Hits](Json.reads[Hits], Json.writes[Hits])
  }
  case class Hits(max_score: Double, hits: List[SearchMeta])
  object SearchResult {
    implicit val format = Format[SearchResult](Json.reads[SearchResult], Json.writes[SearchResult])
  }
  case class SearchResult(hits: Hits, error: Option[String])

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
    val response = ws(_.url(s"$baseUrl/api/_bulk"))
      .withAuth(config.getZincUser, config.getZincPassword, WSAuthScheme.BASIC)
      .post(payload)
    response.map(_.body[String]).map(resp => logger.info(s"bulk resp = $resp"))
  }

  def searchBrands(term: String): Future[List[MarketplaceBrand]] =
    search(term, "matchphrase").map(_.hits.hits.map(_._source))

  def suggestBrands(term: String): Future[List[String]] =
    search(term, "prefix").map(_.hits.hits.map(_._source.brand.name).distinct)

  private def search(term: String, searchType: String): Future[SearchResult] = {
    val payload =
      s"""{
         |    "search_type": "$searchType",
         |    "query": {
         |        "term": "$term"
         |    },
         |    "from": 0,
         |    "max_results": 20,
         |    "_source": []
         |}""".stripMargin
    val response = ws(_.url(s"$baseUrl/api/$ixName/_search"))
      .withAuth(config.getZincUser, config.getZincPassword, WSAuthScheme.BASIC)
      .post(payload)
    response
      .map(_.body[String])
      .map { resp =>
        // logger.info(s"search resp = $resp")
        JsonIo.read[SearchResult](resp)
      }
  }
}
