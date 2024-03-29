package velocorner.search.manual

import cats.effect.{IO, IOApp}
import cats.effect.kernel.Sync
import velocorner.manual.MyLocalConfig
import velocorner.util.JsonIo
import velocorner.api.brand.MarketplaceBrand

object UploadBrandsManual extends IOApp.Simple with BrandsSupport with MyLocalConfig {

  def run: IO[Unit] = for {
    _ <- info("start uploading brands ...")
    _ <- getFeed.use { feed =>
      for {
        brands <- IO(JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz"))
        _ <- info(s"read ${brands.size} markets...")
        normalized = MarketplaceBrand.normalize(brands)
        _ <- Sync[IO].fromFuture(IO(feed.bulk(normalized)))
      } yield ()
    }
    _ <- info("done ...")
  } yield ()
}
