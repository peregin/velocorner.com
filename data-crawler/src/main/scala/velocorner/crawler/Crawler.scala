package velocorner.crawler

import velocorner.api.brand.{Marketplace, ProductDetails}

trait Crawler[F[_]] {

  def products(market: Marketplace, searchTerm: String): F[List[ProductDetails]]
}
