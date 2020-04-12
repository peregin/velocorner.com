package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.model.weather.{ForecastResponse, WeatherResponse}
import velocorner.util.JsonIo
import cats.implicits._

import scala.concurrent.Future

object OpenWeatherFeed {

  val baseUrl = "https://api.openweathermap.org/data/2.5"
}

class OpenWeatherFeed(val config: SecretConfig) extends HttpFeed with LazyLogging {

  // for 5 days
  def forecast(location: String): Future[ForecastResponse] = {
    logger.debug(s"retrieving forecast for $location")
    val response = ws{_.url(s"${OpenWeatherFeed.baseUrl}/forecast")
      .withQueryStringParameters(
        ("q", location),
        ("appid", config.getId("weather")),
        ("units", "metric"),
        ("lang", "en")
      )
      .get()
    }
    response
      .map(_.body)
      .map(JsonIo.read[ForecastResponse])
  }

  def current(location: String): Future[Option[WeatherResponse]] = {
    logger.debug(s"retrieving current weather for $location")
    val response = ws{_.url(s"${OpenWeatherFeed.baseUrl}/weather")
      .withQueryStringParameters(
        ("q", location),
        ("appid", config.getId("weather")),
        ("units", "metric"),
        ("lang", "en")
      )
      .get()
    }
    response.map { res => res.status match {
      case 200 => JsonIo.read[WeatherResponse](res.body).some
      case _ => None
    }}

  }
}
