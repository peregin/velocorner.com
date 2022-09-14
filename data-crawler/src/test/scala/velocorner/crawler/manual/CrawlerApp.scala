package velocorner.crawler.manual

import cats.effect.{IO, IOApp, Resource}
import org.http4s.ember.client.EmberClientBuilder
import velocorner.crawler.CrawlerBikeComponents

object CrawlerApp extends IOApp.Simple {

  override def run: IO[Unit] = (for {
    client <- EmberClientBuilder.default[IO].build
    crawler = new CrawlerBikeComponents[IO](client)
    res <- Resource.eval(crawler.products("sram chain 1x10"))
    _ <- Resource.eval(IO.println(s"search result = $res"))
  } yield ()).useForever
}
