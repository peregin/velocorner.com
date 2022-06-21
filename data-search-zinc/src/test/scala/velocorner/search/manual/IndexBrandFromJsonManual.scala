package velocorner.search.manual

import cats.effect.kernel.Sync
import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.HttpFeed
import velocorner.manual.MyLocalConfig
import velocorner.model.brand.MarketplaceBrand
import velocorner.search.BrandSearch
import velocorner.util.JsonIo

object IndexBrandFromJsonManual extends IOApp.Simple with LazyLogging with MyLocalConfig {

  def info(msg: String): IO[Unit] = IO(logger.info(msg))

  def run: IO[Unit] = for {
    _ <- info("start uploading brands ...")
    config = SecretConfig.load()
    feed = new BrandSearch(config)
    brands <- IO(JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz"))
    _ <- info(s"read ${brands.size} markets...")
    _ <- Sync[IO].fromFuture(IO(feed.bulk(brands)))
    _ = feed.close()
    _ = HttpFeed.shutdown()
  } yield ()
}
