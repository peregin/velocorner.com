package velocorner.feed

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model.weather.{ForecastResponse, WeatherResponse}
import velocorner.util.JsonIo

import scala.concurrent.Future

object OpenWeatherFeed {

  val baseUrl = "https://api.openweathermap.org/data/2.5"
}

class OpenWeatherFeed(val config: SecretConfig) extends HttpFeed with Logging {

  // for 5 days
  def forecast(location: String): Future[ForecastResponse] = {
    log.debug(s"retrieving forecast for $location")
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

  def current(location: String): Future[WeatherResponse] = {
    log.debug(s"retrieving current weather for $location")
    val response = ws{_.url(s"${OpenWeatherFeed.baseUrl}/weather")
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
      .map(JsonIo.read[WeatherResponse])
  }
}
