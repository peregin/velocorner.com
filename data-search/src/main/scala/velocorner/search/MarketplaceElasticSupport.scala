package velocorner.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.api.IndexApi
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexRequest, DeleteIndexRequest, IndexRequest}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import velocorner.model.brand.MarketplaceBrand
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//noinspection TypeAnnotation
trait MarketplaceElasticSupport extends IndexApi with ElasticSupport with LazyLogging {

  lazy val elastic = createElasticClient()

  val ixName = "marketplace"

  def deleteRequest(): DeleteIndexRequest = deleteIndex(ixName)

  def setupRequest(): CreateIndexRequest = createIndex(ixName).mapping(
    properties(
      completionField("brand.name")
    )
  )

  def suggestBrands(term: String): Future[List[String]] = {
    val sugComp = completionSuggestion("mysugg", "brand.name").prefix(term).skipDuplicates(true)
    val res = elastic.execute(search(ixName).suggestions { sugComp } limit 10)
    res.map(_.result.completionSuggestion("mysugg").values.flatMap(_.options).map(_.text).toList)
  }

  def searchBrands(term: String): Future[List[MarketplaceBrand]] = {
    val res = elastic.execute(search(ixName) query term limit 10)
    res.map { r =>
      val markets = r.result.hits.hits.toList.map(h => JsonIo.read[MarketplaceBrand](h.sourceAsString))
      logger.info(s"hits: ${r.result.hits.size}, score: ${r.result.hits.maxScore}")
      markets
    }
  }

  def toIndices(markets: List[MarketplaceBrand]): List[IndexRequest] = {
    markets.map { market =>
      val ixDefinition = indexInto(ixName)
      ixDefinition
        .withId(market.marketplace.name + "/" + market.brand.name)
        .doc(Json.toJson(market).toString())
    }
  }
}
