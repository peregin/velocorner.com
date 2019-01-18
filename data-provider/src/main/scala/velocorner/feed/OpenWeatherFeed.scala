package velocorner.feed

import org.slf4s.Logging
import velocorner.SecretConfig

import scala.concurrent.Await

object OpenWeatherFeed {

  val baseUrl = "https://api.openweathermap.org/data/2.5"
}

// baseUrl/forecast?q=city,iso&appid=x
class OpenWeatherFeed(val config: SecretConfig) extends HttpFeed with Logging {

  def query(location: String): String = {
    val response = ws{_.url(s"${WithingsMeasureFeed.baseUrl}/forecast")
      .withQueryStringParameters(
        ("q", "location"),
        ("appid", config.getId("weather"))
      )
      .get()
    }
    Await.result(response, timeout).body
  }
}
