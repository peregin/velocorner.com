package velocorner.crawler.cache

import cats.effect.Async
import cats.implicits.catsSyntaxOptionId
import com.google.common.cache.CacheBuilder
import scalacache.{cachingF, Cache, Entry, Mode}
import scalacache.guava.GuavaCache

import scala.concurrent.duration.DurationInt

// simple wrapper around scalacache
class InMemoryCache[F[_]: Async, V] {

  val maxSize = 100L
  val ttl = 1.hour

  val guavaCache = CacheBuilder.newBuilder().maximumSize(maxSize).build[String, Entry[V]]
  implicit val cache: Cache[V] = GuavaCache(guavaCache)
  implicit val mode: Mode[F] = CatsEffect.modes.async

  def cacheF(key: String, fun: => F[V]): F[V] = cachingF[F, V]("id", key)(ttl = ttl.some)(fun)
}
