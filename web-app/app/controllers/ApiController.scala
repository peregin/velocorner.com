package controllers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import controllers.auth.AuthChecker
import highcharts._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Duration, LocalDate}
import org.reactivestreams.Subscriber
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.model._
import velocorner.model.weather.{SunriseSunset, WeatherForecast}
import velocorner.storage.OrientDbStorage
import velocorner.util.{CountryIsoUtils, JsonIo, Metrics}
import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.OptionT
import scalaz._
import Scalaz._


/**
 * Created by levi on 06/10/16.
 */
class ApiController @Inject()(val cache: SyncCacheApi, val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with OriginChecker with Metrics {

  val allowedHosts: Seq[String] = connectivity.allowedHosts

  private val logger = Logger.of(this.getClass)

  // def mapped to /api/athletes/statistics
  // current year's progress
  def statistics = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage
    val currentYear = LocalDate.now().getYear

    val result = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athlete statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId).liftM[OptionT]
      yearlyProgress = YearlyProgress.from(dailyProgress)
      aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      currentYearProgress = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)
    } yield currentYearProgress

    result
      .getOrElse(Progress.zero)
      .map(p => Ok(Json.obj("status" ->"OK", "progress" -> Json.toJson(p))))
  }

  // route mapped to /api/athletes/statistics/yearly/:action
  def yearlyStatistics(action: String) = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage

    val result = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athlete yearly statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId).liftM[OptionT]
      yearlyProgress = YearlyProgress.from(dailyProgress)
    } yield yearlyProgress

    result
      .getOrElse(Iterable.empty)
      .map{ yearlyProgress => action.toLowerCase match {
        case "heatmap" => toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress))
        case "distance" => toDistanceSeries(YearlyProgress.aggregate(yearlyProgress))
        case "elevation" => toElevationSeries(YearlyProgress.aggregate(yearlyProgress))
        case other => sys.error(s"not supported action: $other")
      }}
      .map(dataSeries => Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries))))
  }

  // year to date aggregation
  // route mapped to /api/athletes/statistics/ytd/:action
  def ytdStatistics(action: String) = AuthAsyncAction { implicit request =>
    val now = LocalDate.now()
    val storage = connectivity.getStorage

    val result = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athlete year to date $now statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId).liftM[OptionT]
      yearlyProgress = YearlyProgress.from(dailyProgress)
      ytdProgress = yearlyProgress.map(_.ytd(now)).map(ytd =>
        YearlyProgress(ytd.year, Seq(
          DailyProgress(LocalDate.parse(s"${ytd.year}-01-01"), ytd.progress.map(_.progress).foldLeft(Progress.zero)(_ + _)))
        ))
    } yield ytdProgress

    result
      .getOrElse(Iterable.empty)
      .map{ ytdProgress => action.toLowerCase match {
        case "distance" => toDistanceSeries(ytdProgress)
        case "elevation" => toElevationSeries(ytdProgress)
        case other => sys.error(s"not supported action: $other")
      }}
      .map(dataSeries => Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries))))
  }

  // suggestions when searching, workaround until elastic access, use the storage directly
  // route mapped to /api/activities/suggest
  def suggest(query: String) = timed(s"suggest for $query") { AuthAsyncAction { implicit request =>
    logger.debug(s"suggesting for $query")
    val storage = connectivity.getStorage

    val activitiesTF = for {
      account <- OptionT(Future(loggedIn))
      orientDb <- OptionT(Future(storage.isInstanceOf[OrientDbStorage].fold(storage.asInstanceOf[OrientDbStorage].some, None)))
      activities <- orientDb.suggestActivities(query, account.athleteId, 10).liftM[OptionT]
    } yield activities

    activitiesTF
      .getOrElse(Iterable.empty)
      .map{ activities =>
        logger.debug(s"found ${activities.size} suggested activities ...")
        activities.map( a => Json.obj("value" -> a.name, "data" -> JsonIo.write(a)))
      }
      .map(jsonSuggestions => Ok(Json.obj("suggestions" -> jsonSuggestions)))
  }}

  // retrieves the activity with the given id
  // route mapped to /api/activities/:id
  def activity(id: Int) = timed(s"query for activity $id") { AuthAsyncAction { implicit request =>
    logger.debug(s"querying activity $id")
    val resultET = for {
      _ <- EitherT(Future(loggedIn.toRightDisjunction(Forbidden)))
      activity <- EitherT(connectivity.getStorage.getActivity(id).map(_.toRightDisjunction(NotFound)))
    } yield activity

    resultET
      .map(JsonIo.write(_))
      .map(Ok(_))
      .merge
  }}

  // retrieves the weather forecast for a given place
  // route mapped to /api/weather/:location
  def weather(location: String)= timed(s"query weather forecast for $location") { AuthAsyncAction { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryIsoUtils.iso(location)
    val now = DateTime.now() // inject time iterator instead
    val refreshTimeoutInMinutes = 15 // make it configurable instead
    logger.debug(s"collecting weather forecast for [$location] -> [$isoLocation] at $now")

    // if not in storage use a one year old ts to trigger the query
    def lastUpdateTime= OptionT(connectivity.getStorage.getAttribute(isoLocation, "location"))
      .map(DateTime.parse(_, DateTimeFormat.forPattern(DateTimePattern.longFormat)))
      .getOrElse(now.minusYears(1))

    def retrieveAndStore(place: String) = for {
      entries <- connectivity.getWeatherFeed.forecast(place).map(res => res.points.map(w => WeatherForecast(place, w.dt.getMillis, w)))
      _ = logger.info(s"querying latest weather forecast for $place")
      _ <- connectivity.getStorage.storeWeather(entries)
      _ <- connectivity.getStorage.storeAttribute(place,"location", now.toString(DateTimePattern.longFormat))
    } yield entries

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRightDisjunction(BadRequest)))
      lastUpdate <- EitherT.rightT(lastUpdateTime)
      elapsedInMinutes = new Duration(lastUpdate, now).getStandardMinutes // should be more than configurable mins to execute the query
      _ = logger.info(s"last weather update on $place was at $lastUpdate, $elapsedInMinutes minutes ago")
      entries <- EitherT.rightT(if (elapsedInMinutes > refreshTimeoutInMinutes) retrieveAndStore(place) else connectivity.getStorage.listRecentForecast(place))
    } yield entries

    // generate json or xml content
    type transform2Content = Iterable[WeatherForecast] => String
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
  def sunrise(location: String)= timed(s"query sunrise sunset for $location") { AuthAsyncAction { implicit request =>
    // convert city[,country] to city[,isoCountry]
    val isoLocation = CountryIsoUtils.iso(location)
    val now = LocalDate.now.toString
    logger.debug(s"collecting sunrise/sunset times for [$location] -> [$isoLocation] at [$now]")

    def retrieveAndStore: OptionT[Future, SunriseSunset] = for {
      response <- connectivity.getWeatherFeed.current(isoLocation).liftM[OptionT]
      newEntry <- OptionT(Future(response.sys.map(s => SunriseSunset(isoLocation, now, s.sunrise, s.sunset))))
      _ <- Future(connectivity.getStorage.storeSunriseSunset(newEntry)).liftM[OptionT]
    } yield newEntry

    val resultET = for {
      place <- EitherT(Future(Option(isoLocation)
        .filter(_.nonEmpty)
        .toRightDisjunction(BadRequest)))
      // it is in the storage or retrieve it and store it
      sunrise <- OptionT(connectivity.getStorage.getSunriseSunset(place, now))
        .orElse(retrieveAndStore)
        .toRight(NotFound)
    } yield sunrise

    resultET
      .map(JsonIo.write(_))
      .map(Ok(_))
      .merge
  }}

  // WebSocket to update the client
  // try with https://www.websocket.org/echo.html => ws://localhost:9000/ws
  def ws: WebSocket = WebSocket.acceptOrResult[String, String] { rh =>
    rh match {
      case _ if sameOriginCheck(rh) =>
        logger.info(s"ws with request header: $rh")
        val flow = wsFlow(rh)
        Future.successful[Either[Result, Flow[String, String, _]]](Right(flow)).recover{ case e =>
          logger.error("failed to create websocket", e)
          Left(InternalServerError(s"failed to create websocket, ${e.getMessage}"))
        }
      case rejected =>
        logger.error(s"same origin check failed for $rejected")
        Future.successful(Left(Forbidden))
    }
  }

  var counter = 1
  private def wsFlow(rh: RequestHeader): Flow[String, String, NotUsed] = {
    // input, just echo the input
    val in = Sink.foreach[String](println)

    // output, use a publisher
    //val out1 = Source.single("Welcome").concat(Source.maybe)
    val out = Source.fromPublisher((s: Subscriber[_ >: String]) => {
      (1 to 3).foreach { ix =>
        logger.info(s"PUBLISH $counter, ix = $ix")
        s.onNext(s"hello $counter, ix = $ix")
        counter = counter + 1
      }
    }).mapMaterializedValue{a =>
      logger.info(s"CONNECTED $a")
      a
    }.watchTermination() { (_, terminated) =>
      terminated.onComplete(_ => logger.info("DISCONNECTED"))
    }

    Flow.fromSinkAndSource(in, out)
  }
}
