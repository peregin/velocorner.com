package velocorner.search.manual

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import scalaz.std.list._
import scalaz.syntax.traverse.ToTraverseOps
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.search.ElasticSupport
import velocorner.storage.{OrientDbStorage, Storage}
import velocorner.util.FutureInstances._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Simple utility to read the activities from the storage and feed it to elastic.
  */
object BuildElasticFromStorageManual extends App with ElasticSupport with AwaitSupport with LazyLogging with MyMacConfig {

  val bulkSize = 20
  val athleteId = 432909

  val storage = Storage.create("or").asInstanceOf[OrientDbStorage] // re, co, mo, dy, or
  storage.initialize()
  val elastic = localCluster()
  logger.info("initialized...")

  val result = for {
    // needs be created with proper mappings at the beginning, once is created geo point type can't be changed
    ixCreate <- elastic.execute(setup())
    _ = logger.info(s"mapping updated $ixCreate")
    activities <- storage.listActivities(athleteId, activityType = None)
    _ = logger.info(s"indexing ${activities.size} documents ...")
    indices = toIndices(activities).toList
    errors <- indices.sliding(bulkSize, bulkSize)
      .map(chunk => elastic.execute(bulk(chunk).refresh(RefreshPolicy.Immediate)))
      .toList
      .sequenceU
      .map(_.filter(_.isError))
  } yield errors

  val errors = result.await
  logger.info(s"errors ${errors.size}")
  if (errors.nonEmpty) logger.error(s"failed $errors")

  elastic.close()
  storage.destroy()
}
