package velocorner.crawler.manual

import cats.implicits._
import cats.effect.{IO, IOApp, Resource}
import org.http4s.ember.client.EmberClientBuilder
import velocorner.crawler._

object CrawlerApp extends IOApp.Simple {

  override def run: IO[Unit] = (for {
    client <- EmberClientBuilder.default[IO].build
    // new CrawlerGalaxus[IO](client)
    // CrawlerBikeComponents[IO](client)
    // CrawlerChainReactionCycles[IO](client)
    // new CrawlerBikeImport[IO](client)
    // new CrawlerBikester[IO](client)
    crawler = new CrawlerAmazon[IO](client)
    // elite suito-t trainer
    // Garmin Edge 830
    res <- Resource.eval(crawler.products("SRAM XX1", 10))
  } yield res).use { res =>
    val clean = res.map(p => p.copy(market = p.market.copy(url = "", logoUrl = ""), description = none))
    IO.println(s"search result = ${clean.mkString("\n", "\n", "\n")}")
  }
}
