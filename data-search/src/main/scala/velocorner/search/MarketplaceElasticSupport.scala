package velocorner.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.api.IndexApi
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexRequest, DeleteIndexRequest, IndexRequest}
import play.api.libs.json.Json
import velocorner.model.brand.MarketplaceBrand

trait MarketplaceElasticSupport extends IndexApi with ElasticSupport {

  val ixName = "marketplace"

  def delete(): DeleteIndexRequest = deleteIndex(ixName)

  def setup(): CreateIndexRequest = createIndex(ixName).mapping(
    properties(
      completionField("brand.name")
    )
  )

  def toIndices(markets: List[MarketplaceBrand]): List[IndexRequest] = {
    markets.map { market =>
      val ixDefinition = indexInto(ixName)
      ixDefinition
//        .fields(
//          "marketplace_name" -> market.marketplace.name,
//        "marketplace.url" -> market.marketplace.url,
//        "marketplace.logo.url" -> market.marketplace.logoUrl,
//        "brand.name" -> market.brand.name,
//        "url" -> market.url
//        )
        .withId(market.marketplace.name + "/" + market.brand.name)
        .doc(Json.toJson(market).toString())
    }
  }
}
