package controllers

import cats.data.EitherT
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.WebMetrics
import org.joda.time.DateTime
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.GeoPosition
import velocorner.api.weather.CurrentWeather
import velocorner.feed.{WeatherFeed, WeatherFeedClient}
import velocorner.util.{CountryUtils, JsonIo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class WeatherController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  lazy val weatherFeed: WeatherFeed[Future] = new WeatherFeedClient(connectivity.secretConfig)

  def clock(): DateTime = DateTime.now() // inject time iterator instead

  // retrieves the sunrise and sunset information for a given place
  // route mapped to /api/weather/current/:location
  def current(location: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"query current weather for $location") { _ =>
      // convert city[,country] to city[,isoCountry]
      val resultET = for {
        loc <- EitherT[Future, Status, String](Future(Option(location).filter(_.nonEmpty).toRight(BadRequest)))
        // convert city[,country] to city[ ,isoCountry]
        isoLocation = CountryUtils.iso(loc)
        _ = logger.debug(s"collecting current weather for [$location] -> [$isoLocation]")
        current <- EitherT.fromOptionF[Future, Status, CurrentWeather](weatherFeed.current(isoLocation), NotFound)
        // store into the locations storage for suggestions and Windy
        newGeoLocation = GeoPosition(latitude = current.coord.lat, longitude = current.coord.lon)
        _ <- EitherT.liftF[Future, Status, Unit](connectivity.getStorage.getLocationStorage.store(isoLocation, newGeoLocation))
      } yield current

      resultET
        .map(JsonIo.write(_))
        .map(Ok(_))
        .merge
    }
  }
}
