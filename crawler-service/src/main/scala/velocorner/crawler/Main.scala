package velocorner.crawler

import cats.effect.{IO, IOApp, Resource}
import cats.implicits._
import com.comcast.ip4s.IpLiteralSyntax
import fs2.io.net.tls.TLSContext
import org.http4s.HttpApp
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.middleware.{ErrorHandling, RequestLogger}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import velocorner.api.brand.Marketplace
import velocorner.crawler.build.BuildInfo

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val server: Resource[IO, Server] = for {
      implicit0(logger: Logger[IO]) <- Resource.eval(Slf4jLogger.create[IO])

      tlsContext <- TLSContext.Builder.forAsync[IO].insecureResource
      client <- EmberClientBuilder.default[IO].withTLSContext(tlsContext).build
      crawlers = List(
        new CrawlerBikeComponents[IO](client),
        new CrawlerGalaxus[IO](client),
        new CrawlerChainReactionCycles[IO](client),
        new CrawlerBikeImport[IO](client),
        new CrawlerBikester[IO](client)
        // new CrawlerVeloFactory[IO](client)
        // new CrawlerAmazon[IO](client)
        // new CrawlerBike24[IO](client)
      )
      _ <- info(s"possible marketplaces: ${Marketplace.values.map(_.name).mkString("\n", "\n", "\n")} ...")
      _ <- info(s"using crawlers: ${crawlers.map(_.market().name).mkString("\n", "\n", "\n")} ...")

      router = new Router[IO](crawlers)
      defaultServer = EmberServerBuilder.default[IO]
      server <- defaultServer
        .withHost(ip"0.0.0.0")
        .withPort(port"9011")
        .withLogger(logger)
        .withHttpApp(
          httpLogger(logger)(ErrorHandling(router.routes.orNotFound))
        )
        .build
      _ <- info("starting crawler service ...")
      _ <- info(s"built time: ${BuildInfo.buildTime}")
    } yield server
    server.useForever
  }

  def httpLogger(logger: Logger[IO]): HttpApp[IO] => HttpApp[IO] = {
    val logAction = (s: String) => logger.info(s)
    RequestLogger.httpApp(logHeaders = true, logBody = false, logAction = logAction.some)
  }

  def info[F[_]: Logger](s: String): Resource[F, Unit] = Resource.eval(Logger[F].info(s))
}
