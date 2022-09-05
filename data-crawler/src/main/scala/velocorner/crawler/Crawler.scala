package velocorner.crawler

import velocorner.api.brand.{Marketplace, ProductDetails}

trait Crawler[F[_]] {

  def market(): Marketplace

  def products(searchTerm: String): F[List[ProductDetails]]
}
