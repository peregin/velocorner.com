package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.stream.Materializer
import velocorner.SecretConfig
import velocorner.model.weather.Location
import velocorner.util.JsonIo

import scala.concurrent.Future

trait WeatherFeed {

  def countryLocation(ip: String): Future[String]
}

class WeatherLocationFeed(override val config: SecretConfig, override val materializer: Materializer = HttpFeed.materializer)
    extends HttpFeed with LazyLogging with WeatherFeed {

  lazy val baseUrl = config.getWeatherUrl

  override def countryLocation(ip: String): Future[String] =
    ws(_.url(s"$baseUrl/location/ip?ip=$ip").get())
      .map(_.body)
      .map(JsonIo.read[Location])
      .map(_.country)
}
