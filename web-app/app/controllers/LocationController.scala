package controllers

import cats.data.OptionT
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.WebMetrics
import play.api.{Environment, Mode}

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import velocorner.api.GeoLocation
import velocorner.util.{CountryUtils, JsonIo}

import java.net.{Inet4Address, InetAddress}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationController @Inject() (
    val connectivity: ConnectivitySettings,
    environment: Environment,
    components: ControllerComponents
) extends AbstractController(components)
    with WebMetrics {

  // retrieves geo position for the given location
  // route mapped to /api/location/geo/:location
  def geo(location: String) = Action.async {
    timedRequest(s"query geo location for $location") { implicit request =>
      // convert city[,country] to city[,isoCountry]
      val isoLocation = CountryUtils.iso(location)
      val maybeGeoPosition = OptionT(connectivity.getStorage.getLocationStorage.getPosition(isoLocation))
      maybeGeoPosition.map(gp => Ok(JsonIo.write(gp))).getOrElse(NotFound)
    }
  }

  // retrieves the country and capital using a lite-weight database
  // main use case is to show weather forecast and wind direction when opening for the first time the landing page.
  // route mapped to /api/location/ip
  def ip() = Action.async {
    timedRequest(s"query location for ip") { implicit request =>
      val remoteAddress = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress) match {
        case "0:0:0:0:0:0:0:1" if environment.mode == Mode.Dev => "85.1.45.35"
        case "0:0:0:0:0:0:0:1"                                 => "127.0.0.1"
        case other                                             => other
      }
      logger.debug(s"searching for $remoteAddress")
      val result = for {
        countryCode2 <- OptionT(connectivity.getStorage.getLocationStorage.getCountry(remoteAddress))
        capital <- OptionT(Future(CountryUtils.code2Capital.get(countryCode2)))
      } yield GeoLocation(city = capital, country = countryCode2)
      result.map(gl => Ok(JsonIo.write(gl))).getOrElse(NotFound)
    }
  }
}
