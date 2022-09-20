package velocorner.crawler

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.Port
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import velocorner.api.brand.Marketplace

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    val server: Resource[IO, Server] = for {
      implicit0(logger: Logger[IO]) <- Resource.eval(Slf4jLogger.create[IO])
      defaultServer = EmberServerBuilder.default[IO]
      client <- EmberClientBuilder.default[IO].build
      crawlers = List(
        new CrawlerBikeComponents[IO](client)
      )
      _ <- info(s"possible marketplaces: ${Marketplace.values.map(_.name).mkString("\n", "\n", "\n")} ...")
      _ <- info(s"using crawlers: ${crawlers.map(_.market().name).mkString("\n", "\n", "\n")} ...")
      router = new Router[IO](crawlers)
      server <- defaultServer
        .withPort(Port.fromInt(9011).getOrElse(defaultServer.port))
        .withLogger(logger)
        .withHttpApp(router.routes.orNotFound)
        .build
      _ <- info("starting crawler service ...")
    } yield server
    server.useForever
  }

  def info[IO[_]: Logger](s: String): Resource[IO, Unit] = Resource.eval(Logger[IO].info(s))
}
