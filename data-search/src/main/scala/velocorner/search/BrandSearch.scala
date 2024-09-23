package velocorner.search

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.WSAuthScheme
import velocorner.SecretConfig
import velocorner.feed.HttpFeed
import velocorner.api.brand.MarketplaceBrand
import velocorner.util.JsonIo

import java.util.UUID
import scala.concurrent.Future

// noinspection TypeAnnotation
class BrandSearch(val config: SecretConfig) extends HttpFeed with LazyLogging {

  object SearchMeta {
    implicit val format: Format[SearchMeta] = Format[SearchMeta](Json.reads[SearchMeta], Json.writes[SearchMeta])
  }
  case class SearchMeta(_id: String, _source: MarketplaceBrand)
  object Hits {
    implicit val format: Format[Hits] = Format[Hits](Json.reads[Hits], Json.writes[Hits])
  }
  case class Hits(max_score: Double, hits: Option[List[SearchMeta]]) {
    def list(): List[SearchMeta] = hits.getOrElse(Nil)
  }
  object SearchResult {
    implicit val format: Format[SearchResult] = Format[SearchResult](Json.reads[SearchResult], Json.writes[SearchResult])
  }
  case class SearchResult(hits: Hits, error: Option[String])

  object IndexMeta {
    implicit val format: Format[IndexMeta] = Format[IndexMeta](Json.reads[IndexMeta], Json.writes[IndexMeta])
  }
  case class IndexMeta(name: String, doc_num: Long)

  private lazy val baseUrl = config.getZincUrl
  private val ixName = "brand"

  def bulk(brands: List[MarketplaceBrand]): Future[Unit] = {
    logger.debug(s"indexing ${brands.size} brands")
    // Payload - ndjson (newline delimited json) content
    // First line is index action
    // Second line is document data
    val payload = brands
      .map { b =>
        val id = Option(b.toId).filter(_.nonEmpty).getOrElse(UUID.randomUUID().toString)
        val doc = JsonIo.write(b, pretty = false)
        s"""{ "index":{ "_index":"$ixName", "_id": "$id" } }
          |$doc""".stripMargin
      }
      .mkString("\n")
    val response = ws(_.url(s"$baseUrl/api/_bulk"))
      .withAuth(config.getZincUser, config.getZincPassword, WSAuthScheme.BASIC)
      .post(payload)
    response.map(_.body[String]).map(resp => logger.info(s"bulk resp = $resp"))
  }

  def searchBrands(term: String): Future[List[MarketplaceBrand]] =
    search(term, "matchphrase").map(_.hits.list().map(_._source))

  def suggestBrands(term: String): Future[List[String]] =
    search(term, "prefix").map(_.hits.list().map(_._source.brand.name).distinct)

  private def search(term: String, searchType: String): Future[SearchResult] = {
    val payload =
      s"""{
         |    "search_type": "$searchType",
         |    "query": {
         |        "term": "${term.toLowerCase}"
         |    },
         |    "sort_fields": ["-@timestamp"],
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
        JsonIo.read[SearchResult](resp)
      }
      .map { sr =>
        logger.debug(s"$searchType[$term] has ${sr.hits.list().size} results")
        sr
      }
  }

  def countBrands(): Future[Long] = {
    val response = ws(_.url(s"$baseUrl/api/index"))
      .withAuth(config.getZincUser, config.getZincPassword, WSAuthScheme.BASIC)
      .get()
    response
      .map(_.body[String])
      .map { resp =>
        JsonIo.read[List[IndexMeta]](resp)
      }
      .map(_.find(_.name == ixName).map(_.doc_num).getOrElse(0L))
  }

  // ZincSearch version
  def version(): Future[String] = for {
    resp <- ws(_.url(s"$baseUrl/version")).get()
    text = resp.body[String]
    json = Json.parse(text)
  } yield (json \ "version").as[String]
}
