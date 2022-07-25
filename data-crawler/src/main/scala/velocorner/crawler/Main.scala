package velocorner.crawler

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.Port
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{GET, _}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  val crawlerService = HttpRoutes.of[IO] {
    case GET -> Root / "crawler" =>
      Ok("Hello")
  }.orNotFound

  override def run: IO[Unit] = {
    val server: Resource[IO, Server] = for {
      logger <- Resource.eval(Slf4jLogger.create[IO])
      defaultServer = EmberServerBuilder.default[IO]
      server <- defaultServer
        .withPort(Port.fromInt(9011).getOrElse(defaultServer.port))
        .withLogger(logger)
        .withHttpApp(crawlerService)
        .build
      _ <- Resource.eval(logger.info("Starting crawler service ..."))
    } yield server
    server.use(_ => IO.never)
  }
}
