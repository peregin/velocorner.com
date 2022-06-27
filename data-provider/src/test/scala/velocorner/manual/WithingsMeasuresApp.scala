package velocorner.manual

import cats.effect.{ExitCode, IO, IOApp, Resource}
import play.api.libs.oauth
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, WithingsMeasureFeed}
import velocorner.util.CloseableResource

/**
 * Created by levi on 23.10.16.
 */
object WithingsMeasuresApp extends IOApp with CloseableResource with MyLocalConfig {

  lazy val config = SecretConfig.load()

  override def run(args: List[String]): IO[ExitCode] = (for {
    _ <- Resource.fromAutoCloseable(IO(new WithingsMeasureFeed(3112606, oauth.RequestToken(args(0), args(1)), config))).use { feed =>
      val res = feed.listMeasures
      IO.println(s"result is $res")
    }
    _ = HttpFeed.shutdown()
  } yield ()).as(ExitCode.Success)

}
