package controllers

import cats.data.OptionT
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.WebMetrics
import play.api.libs.json.Json

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.util.{CountryUtils, JsonIo}

import scala.concurrent.ExecutionContext.Implicits.global

class LocationController @Inject() (
    val connectivity: ConnectivitySettings,
    components: ControllerComponents
) extends AbstractController(components)
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
}
