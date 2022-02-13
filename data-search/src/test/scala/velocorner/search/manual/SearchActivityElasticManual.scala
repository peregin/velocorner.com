package velocorner.search.manual

import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.AwaitSupport
import velocorner.search.ActivityElasticSupport

import scala.collection.immutable.SortedMap

// search with filtering and sorting
object SearchActivityElasticManual extends App with ActivityElasticSupport with AwaitSupport with LazyLogging {

  val elastic = localCluster()
  logger.info("initialized...")

  val res = elastic
    .execute(
      search(ixName) query "Zermatt"
        bool must(matchQuery("type", "Ride"))
        sortByFieldDesc "start_date"
    )
    .await
  val hits = res.result.hits.hits.toList
  hits.foreach(h => logger.info(s"${SortedMap(h.sourceAsMap.toIndexedSeq: _*)}"))

  elastic.close()
}
