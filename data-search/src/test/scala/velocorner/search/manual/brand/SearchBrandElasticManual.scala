package velocorner.search.manual.brand

import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.AwaitSupport
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.MarketplaceElasticSupport
import velocorner.util.JsonIo

object SearchBrandElasticManual extends App with MarketplaceElasticSupport with AwaitSupport with LazyLogging {

  val elastic = localCluster()
  logger.info("initialized...")

  val res = elastic.execute(search(ixName) query "Schwalbe" limit 10).await
  val hits = res.result.hits.hits.toList.map(h => JsonIo.read[MarketplaceBrand](h.sourceAsString))
  logger.info(s"results: ${hits.mkString("\n", "\n", "\n")}")
  logger.info(s"hits: ${res.result.hits.size}, score: ${res.result.hits.maxScore}")

  val sugText = "sch"
  val sugComp = completionSuggestion("mysugg", "brand.name").prefix(sugText).skipDuplicates(true)
  val res2 = elastic.execute(search(ixName).suggestions { sugComp } limit 20).await
  logger.info(s"suggestions: $res2")
  val suggestions = res2.result.completionSuggestion("mysugg").values.flatMap(_.options).map(_.text)
  logger.info(s"suggestions[$sugText]: $suggestions")

  elastic.close()
}
