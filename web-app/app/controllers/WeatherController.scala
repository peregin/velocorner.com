package controllers

import cats._
import cats.implicits._
import cats.data.{EitherT, OptionT}
import cats.instances.future.catsStdInstancesForFuture
import controllers.util.WebMetrics
import model.highcharts
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Duration}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.api.GeoPosition
import velocorner.api.weather.{CurrentWeather, WeatherForecast}
import velocorner.model._
import velocorner.storage.{AttributeStorage, PsqlDbStorage}
import velocorner.util.CountryUtils.normalize
import velocorner.util.{CountryUtils, JsonIo, WeatherCodeUtils}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class WeatherController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  // keeps the weather data so much time in the cache
  val refreshTimeoutInMinutes = 15

  def clock(): DateTime = DateTime.now() // inject time iterator instead

  // extract the functionality that check the cache and refreshes data based on a expiration time
  def retrieveCacheOrService[T, M[_]: Monad](
      location: String,
      cacheTsKey: String,
      attributeStorage: AttributeStorage[M],
      retrieveAndStoreFromService: String => M[T],
      retrieveFromCache: String => M[T]
  ): M[T] = {
    val now = clock()
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryUtils.iso(location)
    logger.debug(s"collecting weather forecast for [$location] -> [$isoLocation] at $now")

    // if not in storage use a one year old ts to trigger the query
    def lastUpdateM: M[DateTime] = attributeStorage.getAttribute(isoLocation, cacheTsKey).map {
      case Some(timeText) => DateTime.parse(timeText, DateTimeFormat.forPattern(DateTimePattern.longFormat))
      case _              => now.minusYears(1)
    }

    for {
      lastUpdate <- lastUpdateM
      elapsedInMinutes = new Duration(lastUpdate, now).getStandardMinutes
      _ = logger.info(s"last [$cacheTsKey] update on $isoLocation was at $lastUpdate, $elapsedInMinutes minutes ago")
      entry <-
        if (elapsedInMinutes > refreshTimeoutInMinutes) for {
          an <- retrieveAndStoreFromService(isoLocation)
          _ <- attributeStorage.storeAttribute(isoLocation, cacheTsKey, now.toString(DateTimePattern.longFormat))
        } yield an
        else retrieveFromCache(isoLocation)
    } yield entry
  }

  // retrieves the weather forecast for a given place
  // route mapped to /api/weather/forecast/:location
  def forecast(location: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"query weather forecast for $location") { implicit request =>
      val weatherStorage = connectivity.getStorage.getWeatherStorage
      val attributeStorage = connectivity.getStorage.getAttributeStorage // for storing the last update
      val resultET = for {
        loc <- EitherT(Future(Option(location).filter(_.nonEmpty).toRight(BadRequest)))
        wf <- EitherT.right[Status](
          retrieveCacheOrService[List[WeatherForecast], Future](
            loc,
            attributeStorage.forecastTsKey,
            attributeStorage,
            place =>
              for {
                entries <- connectivity.getWeatherFeed
                  .forecast(place)
                  .map(res => res.points.map(w => WeatherForecast(place, w.dt, w)))
                _ = logger.info(s"querying latest weather forecast for $place")
                _ <- weatherStorage.storeRecentForecast(entries)
              } yield entries,
            place => weatherStorage.listRecentForecast(place).map(_.toList)
          )
        )
      } yield wf

      // generate xml content for Meteogram from Highcharts
      resultET
        .map(_.sortBy(_.timestamp))
        .map(wt => highcharts.toMeteoGramXml(wt).toString())
        .map(Ok(_).withCookies(WeatherCookie.create(location)).as("application/xml"))
        .merge
    }
  }

  // retrieves the sunrise and sunset information for a given place
  // route mapped to /api/weather/current/:location
  def current(location: String): Action[AnyContent] = Action.async {
    timedRequest[AnyContent](s"query current weather for $location") { implicit request =>
      // convert city[,country] to city[,isoCountry]
      val weatherStorage = connectivity.getStorage.getWeatherStorage
      val attributeStorage = connectivity.getStorage.getAttributeStorage // for storing the last update
      val resultET = for {
        loc <- EitherT(Future(Option(location).filter(_.nonEmpty).toRight(BadRequest)))
        wf <- EitherT {
          retrieveCacheOrService[Option[CurrentWeather], Future](
            loc,
            attributeStorage.weatherTsKey,
            attributeStorage,
            place =>
              (for {
                response <- OptionT(connectivity.getWeatherFeed.current(place))
                // weather forecast
                currentWeather <- OptionT(
                  Future(
                    for {
                      current <- response.weather.getOrElse(Nil).headOption
                      info <- response.main
                      sunset <- response.sys
                    } yield CurrentWeather(
                      location = place, // city[, country iso 2 letters]
                      timestamp = clock(),
                      bootstrapIcon = WeatherCodeUtils.bootstrapIcon(current.id),
                      current = current,
                      info = info,
                      sunriseSunset = sunset
                    )
                  )
                )
                _ <- OptionT.liftF(weatherStorage.storeRecentWeather(currentWeather))
                // geo location based on place definition used for wind status
                newGeoLocation <- OptionT(Future(response.coord.map(s => GeoPosition(latitude = s.lat, longitude = s.lon))))
                _ <- OptionT.liftF(connectivity.getStorage.getLocationStorage.store(place, newGeoLocation))
              } yield currentWeather).value,
            place => weatherStorage.getRecentWeather(place)
          ).map(_.toRight(NotFound))
        }
      } yield wf

      resultET
        .map(JsonIo.write(_))
        .map(Ok(_))
        .merge
    }
  }

  // suggestions for weather locations, workaround until elastic access, use the storage directly
  // route mapped to /api/weather/suggest
  def suggest(query: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"suggest location for $query") { _ =>
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
      }
    }
}
