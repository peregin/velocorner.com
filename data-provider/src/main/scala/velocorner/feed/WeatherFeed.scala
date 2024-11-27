package velocorner.feed

import cats.implicits.{catsSyntaxOptionId, none}
import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.api.weather.CurrentWeather
import velocorner.util.JsonIo

import scala.concurrent.Future

// client interface to the weather service
trait WeatherFeed[F[_]] {

  def current(location: String): F[Option[CurrentWeather]]
}

class WeatherFeedClient(override val config: SecretConfig) extends HttpFeed with LazyLogging with WeatherFeed[Future] {

  private lazy val baseUrl = config.getWeatherUrl

  override def current(location: String): Future[Option[CurrentWeather]] =
    ws(_.url(s"$baseUrl/weather/current/$location").get())
      .flatMap { res =>
        val body = res.body
        res.status match {
          case 200 =>
            Future.successful(JsonIo.read[CurrentWeather](body).some)
          case 404 =>
            Future.successful(none)
          case other =>
            Future.failed(new IllegalArgumentException(s"unknown response[$other], body is: $body"))
        }
      }
}
