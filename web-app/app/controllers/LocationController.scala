package controllers

import cats.data.OptionT
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.WebMetrics
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import velocorner.util.{CountryIsoUtils, JsonIo}

import scala.concurrent.ExecutionContext.Implicits.global


class LocationController @Inject()(val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with WebMetrics {

  // retrieves geo position for the given location
  // route mapped to /api/location/geo/:location
  def geo(location: String)= Action.async { timedRequest(s"query geo location for $location") { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryIsoUtils.iso(location)
    val maybeGeoPosition = OptionT(connectivity.getStorage.getLocationStorage.getPosition(isoLocation))
    maybeGeoPosition.map(gp => Ok(JsonIo.write(gp))).getOrElse(NotFound)
  }}
}
