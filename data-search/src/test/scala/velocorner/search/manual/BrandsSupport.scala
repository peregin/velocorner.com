package velocorner.search.manual

import cats.effect.{IO, Resource}
import com.typesafe.scalalogging.LazyLogging
import velocorner.feed.HttpFeed
import velocorner.search.BrandSearch
import velocorner.SecretConfig

// utilities
trait BrandsSupport extends LazyLogging {

  def info(msg: String): IO[Unit] = IO(logger.info(msg))

  def getFeed: Resource[IO, BrandSearch] =
    Resource.make(IO(new BrandSearch(SecretConfig.load())))(feed => IO(feed.close()) *> IO(HttpFeed.shutdown()))
}
