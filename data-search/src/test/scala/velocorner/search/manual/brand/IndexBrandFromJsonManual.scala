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
object IndexBrandFromJsonManual extends IOApp.Simple with MarketplaceElasticSupport with AwaitSupport with LazyLogging with MyLocalConfig {

  val bulkSize = 200

  def info(msg: String): IO[Unit] = IO(logger.info(msg))

  def run: IO[Unit] = for {
    _ <- info("start uploading brands ...")
    markets <- IO(JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz"))
    _ <- info(s"read ${markets.size} markets...")
    _ <- Async[IO].fromFuture(IO(elastic.execute(deleteRequest())))
    _ <- info("ix deleted ...")
    ixCreate <- Async[IO].fromFuture(IO(elastic.execute(setupRequest())))
    _ <- info(s"index updated $ixCreate")
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
    _ <- IO.whenA(errors.nonEmpty)(info(s"failed with $errors"))
    // use resource
    _ <- IO(elastic.close())
    _ <- info("bye ...")
  } yield ()
}
