package velocorner.feed

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.model.weather.ForecastResponse
import velocorner.util.JsonIo

import scala.concurrent.Future

object OpenWeatherFeed {

  val baseUrl = "https://api.openweathermap.org/data/2.5"
}

class OpenWeatherFeed(val config: SecretConfig) extends HttpFeed with Logging {

  def query(location: String): Future[ForecastResponse] = {
    val response = ws{_.url(s"${OpenWeatherFeed.baseUrl}/forecast")
      .withQueryStringParameters(
        ("q", location),
        ("appid", config.getId("weather")),
        ("units", "metric"),
        ("lang", "en")
      )
      .get()
    }
    response.map(_.body).map(JsonIo.read[ForecastResponse])
  }
}
