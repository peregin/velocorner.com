package velocorner.crawler

import cats.effect.Async
import cats.implicits._
import fs2.text
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.{Header, Headers, Method, Uri}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString
import velocorner.api.Money
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}
import velocorner.api.brand.Marketplace.{ChainReactionCycles, Galaxus}
import velocorner.crawler.CrawlerGalaxus.SearchResponse

import java.net.URLEncoder

class CrawlerChainReactionCycles[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = ChainReactionCycles

  override def products(searchTerm: String): F[List[ProductDetails]] = {
    val limit = 5
    val search = URLEncoder.encode(searchTerm, "UTF-8")
    val uri = s"https://www.chainreactioncycles.com/s?q=$search&cat=direct"
    for {
      res <- client.get(uri)(_.body.through(text.utf8.decode).compile.string)
      _ = println(res)
    } yield List.empty
  }
}
