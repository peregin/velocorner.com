package controllers

import highcharts._
import javax.inject.Inject
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Duration, LocalDate}
import play.api.mvc.{AbstractController, ControllerComponents}
import scalaz.Scalaz._
import scalaz.{OptionT, _}
import velocorner.api.weather.{DailyWeather, SunriseSunset, WeatherForecast}
import velocorner.model._
import velocorner.util.{CountryIsoUtils, JsonIo, Metrics}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class WeatherController @Inject()(val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with Metrics {


  // retrieves the weather forecast for a given place
  // route mapped to /api/weather/:location
  def forecast(location: String)= timed(s"query weather forecast for $location") { Action.async { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryIsoUtils.iso(location)
    val now = DateTime.now() // inject time iterator instead
    val refreshTimeoutInMinutes = 15 // make it configurable instead
    val weatherStorage = connectivity.getStorage.getWeatherStorage
    val attributeStorage = connectivity.getStorage.getAttributeStorage
    logger.debug(s"collecting weather forecast for [$location] -> [$isoLocation] at $now")

    // if not in storage use a one year old ts to trigger the query
    def lastUpdateTime = OptionT(attributeStorage.getAttribute(isoLocation, "location"))
      .map(DateTime.parse(_, DateTimeFormat.forPattern(DateTimePattern.longFormat)))
      .getOrElse(now.minusYears(1))

    def retrieveAndStore(place: String) = for {
      entries <- connectivity.getWeatherFeed.forecast(place).map(res => res.points.map(w => WeatherForecast(place, w.dt.getMillis, w)))
      _ = logger.info(s"querying latest weather forecast for $place")
      _ <- weatherStorage.storeWeather(entries)
      _ <- attributeStorage.storeAttribute(place,"location", now.toString(DateTimePattern.longFormat))
    } yield entries

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRightDisjunction(BadRequest)))
      lastUpdate <- EitherT.rightT(lastUpdateTime)
      elapsedInMinutes = new Duration(lastUpdate, now).getStandardMinutes // should be more than configurable mins to execute the query
      _ = logger.info(s"last weather update on $place was at $lastUpdate, $elapsedInMinutes minutes ago")
      entries <- EitherT.rightT(if (elapsedInMinutes > refreshTimeoutInMinutes) retrieveAndStore(place) else weatherStorage.listRecentForecast(place))
    } yield entries

    // generate json or xml content
    type transform2Content = List[WeatherForecast] => String
    val contentGenerator: transform2Content = request.getQueryString("mode") match {
      case Some("xml") => wt => toMeteoGramXml(wt).toString()
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
  def sunrise(location: String)= timed(s"query sunrise sunset for $location") { Action.async { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryIsoUtils.iso(location)
    val now = LocalDate.now.toString
    val weatherStorage = connectivity.getStorage.getWeatherStorage
    logger.debug(s"collecting sunrise/sunset times for [$location] -> [$isoLocation] at [$now]")

    def retrieveAndStore: OptionT[Future, SunriseSunset] = for {
      response <- OptionT(connectivity.getWeatherFeed.current(isoLocation))
      newEntry <- OptionT(Future(response.sys.map(s => SunriseSunset(isoLocation, now, s.sunrise, s.sunset))))
      _ <- weatherStorage.storeSunriseSunset(newEntry).liftM[OptionT]
    } yield newEntry

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRightDisjunction(BadRequest)))
      // it is in the storage or retrieve it and store it
      sunrise <- OptionT(weatherStorage.getSunriseSunset(place, now))
        .orElse(retrieveAndStore)
        .toRight(NotFound)
    } yield sunrise

    resultET
      .map(JsonIo.write(_))
      .map(Ok(_))
      .merge
  }}
}
