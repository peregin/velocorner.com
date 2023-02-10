package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import velocorner.api.brand.{Marketplace, ProductDetails}
import velocorner.SecretConfig
import velocorner.util.JsonIo

import java.net.URLEncoder
import scala.concurrent.Future

// client interface to the weather service
trait WeatherFeed {

  def forecast(location: String): Future[String]
}

class CurrentWeatherFeed(override val config: SecretConfig) extends HttpFeed with LazyLogging with WeatherFeed {

  lazy val baseUrl = config.getWeatherUrl

  override def forecast(location: String): Future[String] =
    ws(_.url(s"$baseUrl/weather/forecast/${URLEncoder.encode(location, "UTF-8")}").get())
      .map(_.body)
}
