package controllers

import controllers.util.WebMetrics
import javax.inject.Inject
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Duration, LocalDate}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.weather.{DailyWeather, SunriseSunset, WeatherForecast}
import velocorner.model._
import velocorner.util.{CountryUtils, JsonIo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.data.{EitherT, OptionT}
import cats.implicits._
import cats.instances.future.catsStdInstancesForFuture
import model.highcharts
import play.api.libs.json.Json
import velocorner.api.GeoPosition
import velocorner.storage.{OrientDbStorage, PsqlDbStorage}
import velocorner.util.CountryUtils.normalize


class WeatherController @Inject()(val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with WebMetrics {


  // retrieves the weather forecast for a given place
  // route mapped to /api/weather/:location
  def forecast(location: String)= Action.async { timedRequest(s"query weather forecast for $location") { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryUtils.iso(location)
    val now = DateTime.now() // inject time iterator instead
    val refreshTimeoutInMinutes = 15 // make it configurable instead
    val weatherStorage = connectivity.getStorage.getWeatherStorage
    val attributeStorage = connectivity.getStorage.getAttributeStorage
    logger.debug(s"collecting weather forecast for [$location] -> [$isoLocation] at $now")

    // if not in storage use a one year old ts to trigger the query
    def lastUpdateTime: Future[DateTime] = attributeStorage.getAttribute(isoLocation, "location").map {
      case Some(timeText) => DateTime.parse(timeText, DateTimeFormat.forPattern(DateTimePattern.longFormat))
      case _ => now.minusYears(1)
    }

    def retrieveAndStore(place: String): Future[List[WeatherForecast]] = for {
      entries <- connectivity.getWeatherFeed.forecast(place).map(res => res.points.map(w => WeatherForecast(place, w.dt.getMillis, w)))
      _ = logger.info(s"querying latest weather forecast for $place")
      _ <- weatherStorage.storeWeather(entries)
      _ <- attributeStorage.storeAttribute(place,"location", now.toString(DateTimePattern.longFormat))
    } yield entries

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRight(BadRequest)))
      lastUpdate <- EitherT.right[Status](lastUpdateTime)
      elapsedInMinutes = new Duration(lastUpdate, now).getStandardMinutes // should be more than configurable minutes to execute the query
      _ = logger.info(s"last weather update on $place was at $lastUpdate, $elapsedInMinutes minutes ago")
      entries <- EitherT.right[Status](if (elapsedInMinutes > refreshTimeoutInMinutes) retrieveAndStore(place) else weatherStorage.listRecentForecast(place))
    } yield entries

    // generate json or xml content
    type transform2Content = List[WeatherForecast] => String
    val contentGenerator: transform2Content = request.getQueryString("mode") match {
      case Some("xml") => wt => highcharts.toMeteoGramXml(wt).toString()
      case _ => wt => JsonIo.write(DailyWeather.list(wt))
    }

    resultET
      .map(_.toList.sortBy(_.timestamp))
      .map(contentGenerator)
      .map(Ok(_).withCookies(WeatherCookie.create(location)))
      .merge
  }}

  // retrieves the sunrise and sunset information for a given place
  // route mapped to /api/sunrise/:location
  def sunrise(location: String)= Action.async { timedRequest(s"query sunrise sunset for $location") { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryUtils.iso(location)
    val now = LocalDate.now.toString
    val weatherStorage = connectivity.getStorage.getWeatherStorage
    logger.debug(s"collecting sunrise/sunset times for [$location] -> [$isoLocation] at [$now]")

    // store sunrise/sunset and location geo position
    def retrieveAndStore: OptionT[Future, SunriseSunset] = for {
      response <- OptionT(connectivity.getWeatherFeed.current(isoLocation))
      newSunriseSunset <- OptionT(Future(response.sys.map(s => SunriseSunset(isoLocation, now, s.sunrise, s.sunset))))
      _ <- OptionT.liftF(weatherStorage.storeSunriseSunset(newSunriseSunset))
      newGeoLocation <- OptionT(Future(response.coord.map(s => GeoPosition(latitude = s.lat, longitude = s.lon))))
      _ <- OptionT.liftF(connectivity.getStorage.getLocationStorage.store(isoLocation, newGeoLocation))
    } yield newSunriseSunset

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRight(BadRequest)))
      // it is in the storage or retrieve it and store it
      sunrise <- OptionT(timedFuture("storage query sunrise sunset")(weatherStorage.getSunriseSunset(place, now)))
        .orElse(retrieveAndStore)
        .toRight(NotFound)
    } yield sunrise

    resultET
      .map(JsonIo.write(_))
      .map(Ok(_))
      .merge
  }}

  // suggestions for weather locations, workaround until elastic access, use the storage directly
  // route mapped to /api/weather/suggest
  def suggest(query: String): Action[AnyContent] =
    Action.async { timedRequest(s"suggest location for $query") { implicit request =>

      logger.debug(s"suggesting for $query")
      val storage = connectivity.getStorage
      val suggestionsF = storage match {
        case psqlDb: PsqlDbStorage => psqlDb.getWeatherStorage.suggestLocations(query)
        case other =>
          logger.warn(s"$other is not supporting location suggestions")
          Future(Iterable.empty)
      }

      suggestionsF
        .map { suggestions =>
          val normalized = normalize(suggestions)
          logger.debug(s"found ${suggestions.size} suggested locations, normalized to ${normalized.size} ...")
          normalized
        }
        .map(jsonSuggestions => Ok(Json.obj("suggestions" -> jsonSuggestions)))
    }}
}
