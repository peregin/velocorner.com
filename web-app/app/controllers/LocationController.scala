package controllers

import cats.data.OptionT
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.{RemoteIp, WebMetrics}
import play.api.Environment
import play.api.libs.json.Json

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.GeoLocation
import velocorner.util.{CountryUtils, JsonIo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationController @Inject() (
    val connectivity: ConnectivitySettings,
    environment: Environment,
    components: ControllerComponents
) extends AbstractController(components)
    with RemoteIp
    with WebMetrics {

  // retrieves geo position for the given location
  // route mapped to /api/location/suggest
  // location is query parameter
  def suggest(query: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"suggest location for $query") { _ =>
      logger.debug(s"suggesting locations for $query")
      val storage = connectivity.getStorage.getLocationStorage
      val res = for {
        suggestions <- storage.suggestLocations(query)
      } yield CountryUtils.normalize(suggestions).map(CountryUtils.beautify)
      res.map(jsonSuggestions => Ok(Json.obj("suggestions" -> jsonSuggestions)))
    }
  }

  // retrieves geo position for the given location
  // route mapped to /api/location/geo/:location
  def geo(location: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"query geo location for $location") { _ =>
      // convert city[,country] to city[,isoCountry]
      val isoLocation = CountryUtils.iso(location)
      val maybeGeoPosition = OptionT(connectivity.getStorage.getLocationStorage.getPosition(isoLocation))
      maybeGeoPosition.map(gp => Ok(JsonIo.write(gp))).getOrElse(NotFound)
    }
  }

  // retrieves the country and capital using a lite-weight database
  // main use case is to show weather forecast and wind direction when opening for the first time the landing page.
  // route mapped to /api/location/ip
  def ip(): Action[AnyContent] = Action.async {
    timedRequest[AnyContent]("query location (iso2 country code and capital) for ip") { implicit request =>
      val remoteAddress = detectIp(request, environment)
      val result = for {
        countryCode2 <- OptionT(connectivity.getStorage.getLocationStorage.getCountry(remoteAddress))
        capital <- OptionT(Future(CountryUtils.code2Capital.get(countryCode2)))
      } yield GeoLocation(city = capital, country = countryCode2)
      result.map(gl => Ok(JsonIo.write(gl))).getOrElse(NotFound)
    }
  }
}
