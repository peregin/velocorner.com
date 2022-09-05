package velocorner.crawler

import org.http4s.client.Client
import velocorner.api.brand.Marketplace.BikeComponents
import velocorner.api.brand.{Marketplace, ProductDetails}

class CrawlerBikeComponents[F[_]](client: Client[F]) extends Crawler[F] {

  override def market(): Marketplace = BikeComponents

  override def products(searchTerm: String): F[List[ProductDetails]] = ???
}
