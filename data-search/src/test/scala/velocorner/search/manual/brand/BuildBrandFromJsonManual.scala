package velocorner.search.manual.brand

import cats.effect.{Async, IO, IOApp}
import cats.implicits._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import mouse.all._
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.MarketplaceElasticSupport
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global

/** Simple utility to read marketplaces from file and feed it to elastic.
  */
object BuildBrandFromJsonManual extends IOApp.Simple with MarketplaceElasticSupport with AwaitSupport with LazyLogging with MyLocalConfig {

  val bulkSize = 200

  def run: IO[Unit] = for {
    _ <- IO(logger.info("start uploading brands ..."))
    markets <- IO(JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz"))
    _ <- IO(logger.info(s"read ${markets.size} markets..."))
    _ <- Async[IO].fromFuture(IO(elastic.execute(deleteRequest())))
    _ <- IO(logger.info("ix deleted ..."))
    ixCreate <- Async[IO].fromFuture(IO(elastic.execute(setupRequest())))
    _ <- IO(logger.info(s"index updated $ixCreate"))
    indices = toIndices(markets)
    errors <- indices
      .sliding(bulkSize, bulkSize)
      .zipWithIndex
      .map { case (chunk, ix) =>
        logger.info(s"bulk $ix indexing ...")
        val res = elastic.execute(bulk(chunk).refresh(RefreshPolicy.Immediate))
        Async[IO].fromFuture(IO(res <| (_.onComplete(_ => logger.info(s"bulk $ix done ...")))))
      }
      .toList
      .parSequence
      .map(_.filter(_.isError))
    _ <- IO(if (errors.nonEmpty) logger.error(s"failed with $errors") else logger.info("done ..."))
    // use resource
    _ <- IO(elastic.close())
    _ <- IO(logger.info("bye ..."))
  } yield ()
}
