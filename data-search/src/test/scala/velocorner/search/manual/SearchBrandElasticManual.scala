package velocorner.search.manual

import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.AwaitSupport
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.MarketplaceElasticSupport
import velocorner.util.JsonIo

object SearchBrandElasticManual extends App with MarketplaceElasticSupport with AwaitSupport with LazyLogging {

  val elastic = localCluster()
  logger.info("initialized...")

  val res = elastic
    .execute(search(ixName) query "Schwalbe")
    .await
  val hits = res.result.hits.hits.toList.map(h => JsonIo.read[MarketplaceBrand](h.sourceAsString))
  logger.info(s"results: ${hits.mkString("\n", "\n", "\n")}")
  logger.info(s"hits: ${res.result.hits.size}, score: ${res.result.hits.maxScore}")

  elastic.close()
}
