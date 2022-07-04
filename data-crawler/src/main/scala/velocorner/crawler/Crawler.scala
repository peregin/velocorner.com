package velocorner.crawler

import velocorner.model.brand.Brand

trait Crawler[F[_]] {

  def brands(): F[List[Brand]]

  def products(): F[Unit]

  def products(searchTerm: String): F[Unit]
}
