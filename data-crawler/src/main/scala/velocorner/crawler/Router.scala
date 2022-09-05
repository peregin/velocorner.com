package velocorner.crawler

import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class Router[F[_]: Concurrent](crawlers: List[Crawler[F]]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes
    .of[F] { case GET -> Root / "search" / term =>
      Ok(s"Hello, looking for $term")
    }
}
