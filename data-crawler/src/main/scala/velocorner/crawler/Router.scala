package velocorner.crawler

import cats.Parallel
import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import velocorner.api.brand.ProductDetails
import velocorner.crawler.cache.InMemoryCache
import velocorner.crawler.model._

class Router[F[_]: Async: Parallel: Logger](crawlers: List[Crawler[F]]) extends Http4sDsl[F] {

  private val cache = new InMemoryCache[F, List[ProductDetails]]()

  private def search(term: String): F[List[ProductDetails]] = for {
    _ <- Logger[F].info(s"searching for $term...")
    suggestions <- crawlers.parTraverse{c =>
      c.products(term).handleErrorWith { e =>
        Logger[F].error(e)("unable to crawl") *> List.empty[ProductDetails].pure[F]
      }
    }.map(_.flatten)
    _ <- Logger[F].info(s"found ${suggestions.size} results...")
  } yield suggestions

  val routes: HttpRoutes[F] = HttpRoutes
    .of[F] {
      case GET -> Root / "search" / term =>
        for {
          suggestions <- cache.cacheF(term, search(term))
          res <- Ok(suggestions)
        } yield res

      case GET -> Root / "supported" =>
        for {
          res <- Ok(crawlers.map(_.market()))
        } yield res
    }
}
