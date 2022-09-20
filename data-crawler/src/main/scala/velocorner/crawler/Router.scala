package velocorner.crawler

import cats.Parallel
import cats.effect.Async
import cats.implicits._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import model._

class Router[F[_]: Async: Parallel: Logger](crawlers: List[Crawler[F]]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes
    .of[F] { case GET -> Root / "search" / term =>
      for {
        _ <- Logger[F].info(s"searching for $term...")
        suggestions <- crawlers.parTraverse(_.products(term)).map(_.flatten)
        _ <- Logger[F].info(s"found ${suggestions.size} results...")
        res <- Ok(suggestions)
      } yield res
    }
}
