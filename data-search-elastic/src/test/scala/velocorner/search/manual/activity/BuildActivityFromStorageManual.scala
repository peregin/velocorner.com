package velocorner.search.manual.activity

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.search.ActivityElasticSupport
import velocorner.storage.Storage

import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import velocorner.SecretConfig

/**
 * Simple utility to read the activities from the storage and feed it to elastic.
 */
object BuildActivityFromStorageManual extends App with ActivityElasticSupport with AwaitSupport with LazyLogging with MyLocalConfig {

  override val config = SecretConfig.load()

  val bulkSize = 20
  val athleteId = 432909

  val storage = Storage.create("ps") // .asInstanceOf[OrientDbStorage] // re, co, mo, dy, or
  storage.initialize()
  val elastic = createElasticClient()
  logger.info("initialized...")

  val result = for {
    // needs be created with proper mappings at the beginning, once is created geo point type can't be changed
    ixCreate <- elastic.execute(setup())
    _ = logger.info(s"mapping updated $ixCreate")
    activities <- storage.listAllActivities(athleteId, "Ride")
    _ = logger.info(s"indexing ${activities.size} documents ...")
    indices = toIndices(activities).toList
    errors <- indices
      .sliding(bulkSize, bulkSize)
      .map(chunk => elastic.execute(bulk(chunk).refresh(RefreshPolicy.Immediate)))
      .toList
      .sequence
      .map(_.filter(_.isError))
  } yield errors

  val errors = result.await
  logger.info(s"errors ${errors.size}")
  if (errors.nonEmpty) logger.error(s"failed $errors")

  elastic.close()
  storage.destroy()
}
