package velocorner.search.manual

import cats.effect.{IO, IOApp}

object IndexBrandFromJsonManual extends IOApp.Simple {

  def run: IO[Unit] = IO.println("indexing...")
}
