package velocorner.crawler.manual

import cats.implicits._
import cats.effect.{IO, IOApp, Resource}
import fs2.io.net.tls.TLSContext
import org.http4s.client.middleware.RequestLogger
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import velocorner.crawler._

object CrawlerApp extends IOApp.Simple {

  val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = (for {
    tlsContext <- TLSContext.Builder.forAsync[IO].insecureResource
    rawClient <- EmberClientBuilder.default[IO].withTLSContext(tlsContext).withLogger(logger).build
    client = RequestLogger(logHeaders = true, logBody = true)(rawClient)
    // crawler = new CrawlerGalaxus[IO](client)
    crawler = new CrawlerBikeComponents[IO](client)
    // crawler = new CrawlerChainReactionCycles[IO](client)
    // crawler = new CrawlerBikeImport[IO](client)
    // crawler = new CrawlerBikester[IO](client)
    // crawler = new CrawlerAmazon[IO](client)
    //crawler = new CrawlerBike24[IO](client)
    //crawler = new CrawlerVeloFactory[IO](client)

    // elite suito-t trainer
    // Garmin Edge 830
    // vittoria graphene 2.0
    _ <- Resource.eval(logger.info(s"connecting to ${crawler.market().name}"))
    res <- Resource.eval(crawler.products("SRAM xx1", 10))
  } yield res).use { res =>
    val clean = res.map(p => p.copy(market = p.market.copy(url = "", logoUrl = ""), description = none))
    IO.println(s"search result = ${clean.mkString("\n", "\n", "\n")}")
  }
}
