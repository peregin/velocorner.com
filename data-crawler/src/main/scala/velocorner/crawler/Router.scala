package velocorner.crawler

import cats.Parallel
import cats.effect.Async
import cats.implicits._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import velocorner.api.Money
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}

class Router[F[_]: Async: Parallel: Logger](crawlers: List[Crawler[F]]) extends Http4sDsl[F] {

  // models are defined in the data-provider module
  // setup codecs to encode in json with circe
  implicit val codecMarket: Codec[Marketplace] = deriveCodec
  implicit val codecMoney: Codec[Money] = deriveCodec
  implicit val codecBrand: Codec[Brand] = deriveCodec
  implicit val codec: Codec[ProductDetails] = deriveCodec

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
