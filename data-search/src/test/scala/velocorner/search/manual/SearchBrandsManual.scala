package velocorner.search.manual

import cats.effect.{IO, IOApp}
import cats.effect.kernel.Sync
import velocorner.manual.MyLocalConfig

object SearchBrandsManual extends IOApp.Simple with BrandsSupport with MyLocalConfig {
  val searchTerm = "ass"
  val suggestionText = "ass"

  override def run: IO[Unit] = for {
    _ <- info("searching ...")
    _ <- getFeed.use { feed =>
      for {
        hits <- Sync[IO].fromFuture(IO(feed.searchBrands(searchTerm)))
        _ <- info(s"results[$searchTerm]: ${hits.mkString("\n", "\n", "\n")}")

        suggestions <- Sync[IO].fromFuture(IO(feed.suggestBrands(suggestionText)))
        _ <- info(s"suggestions[$suggestionText]: $suggestions")

        count <- Sync[IO].fromFuture(IO(feed.countBrands()))
        _ <- info(s"count: $count")

        version <- Sync[IO].fromFuture(IO(feed.version()))
        _ <- info(s"version: $version")
      } yield ()
    }
    _ <- info("done ...")
  } yield ()
}
