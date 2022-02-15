package velocorner.search.manual.brand

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.MarketplaceElasticSupport
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.implicits._
import mouse.all._

/** Simple utility to read marketplaces from file and feed it to elastic.
  */
object BuildBrandFromJsonManual extends App with MarketplaceElasticSupport with AwaitSupport with LazyLogging with MyLocalConfig {

  val bulkSize = 200

  val markets = JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz")
  logger.info(s"read ${markets.size} markets...")

  val elastic = localCluster()
  logger.info("elastic cluster initialized...")

  val result = for {
    _ <- elastic.execute(delete()).recover { err =>
      logger.warn(s"delete failed: $err")
      Future.unit
    }
    ixCreate <- elastic.execute(setup())
    _ = logger.info(s"index updated $ixCreate")
    indices = toIndices(markets)
    errors <- indices
      .sliding(bulkSize, bulkSize)
      .zipWithIndex
      .map { case (chunk, ix) =>
        val res = elastic.execute(bulk(chunk).refresh(RefreshPolicy.Immediate))
        res <| (_.onComplete(_ => logger.info(s"bulk $ix done ...")))
      }
      .toList
      .sequence
      .map(_.filter(_.isError))
  } yield errors

  val errors = result.await
  logger.info(s"errors ${errors.size}")
  if (errors.nonEmpty) logger.error(s"failed with $errors") else logger.info("done...")

  elastic.close()
}
