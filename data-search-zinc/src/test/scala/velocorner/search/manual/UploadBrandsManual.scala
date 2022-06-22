package velocorner.search.manual

import cats.effect.{IO, IOApp}
import cats.effect.kernel.Sync
import velocorner.manual.MyLocalConfig
import velocorner.model.brand.MarketplaceBrand
import velocorner.util.JsonIo

object UploadBrandsManual extends IOApp.Simple with BrandsSupport with MyLocalConfig {

  def run: IO[Unit] = for {
    _ <- info("start uploading brands ...")
    _ <- getFeed.use { feed =>
      for {
        brands <- IO(JsonIo.readFromGzipResource[List[MarketplaceBrand]]("/markets.json.gz"))
        _ <- info(s"read ${brands.size} markets...")
        _ <- Sync[IO].fromFuture(IO(feed.bulk(brands)))
      } yield ()
    }
    _ <- info("done ...")
  } yield ()
}
