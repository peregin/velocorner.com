package velocorner.crawler

import cats.implicits._
import cats.Parallel
import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class Router[F[_]: Async: Parallel](crawlers: List[Crawler[F]]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes
    .of[F] { case GET -> Root / "search" / term =>
      for {
        suggestions <- crawlers.parTraverse(_.products(term))
        res2 <- Ok(s"Hello, looking for $term")
      } yield res2
    }
}
