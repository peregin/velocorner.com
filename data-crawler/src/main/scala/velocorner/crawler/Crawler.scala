package velocorner.crawler

import velocorner.model.brand.Marketplace

trait Crawler[F[_]] {

  def products(market: Marketplace, searchTerm: String): F[Unit]
}
