package velocorner.search.manual.brand

import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.search.MarketplaceElasticSupport
import velocorner.SecretConfig

import scala.concurrent.ExecutionContext.Implicits.global

object SearchBrandElasticManual extends App with MarketplaceElasticSupport with AwaitSupport with LazyLogging with MyLocalConfig {

  override val config = SecretConfig.load()
  val searchTerm = "Schwalbe"
  val suggestionText = "sch"

  logger.info("initialized...")

  (for {
    hits <- searchBrands(searchTerm)
    _ = logger.info(s"results[$searchTerm]: ${hits.mkString("\n", "\n", "\n")}")
    suggestions = suggestBrands(suggestionText).await
    _ = logger.info(s"suggestions[$suggestionText]: $suggestions")
    _ = elastic.close()
  } yield ()).await

}
