package controllers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import controllers.auth.AuthChecker
import highcharts._
import javax.inject.Inject
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Duration, LocalDate}
import org.reactivestreams.Subscriber
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.model._
import velocorner.model.weather.WeatherForecast
import velocorner.storage.OrientDbStorage
import velocorner.util.{CountryIsoUtils, JsonIo, Metrics}

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

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
    val maybeAccount = loggedIn
    logger.info(s"athlete statistics for ${maybeAccount.map(_.displayName)}")

    val result = Try {
      val storage = connectivity.getStorage
      val currentYear = LocalDate.now().getYear
      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearProgress = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)
      Ok(Json.obj("status" ->"OK", "progress" -> Json.toJson(currentYearProgress)))
    }

    Future.successful(result.getOrElse(InternalServerError))
  }

  // def mapped to /api/athletes/statistics/yearly/:action
//  @ApiOperation(value = "List yearly series for the logged in athlete",
//    notes = "Returns the yearly series",
//    responseContainer = "List",
//    response = classOf[highcharts.DailySeries],
//    httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 404, message = "Invalid action"),
//    new ApiResponse(code = 500, message = "Internal error")))
  def yearlyStatistics(action: String) = AuthAsyncAction {
    implicit request =>

    val maybeAccount = loggedIn
    logger.info(s"athlete yearly statistics for ${maybeAccount.map(_.displayName)}")

    val result = Try {
      val storage = connectivity.getStorage
      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)

      val dataSeries = action.toLowerCase match {
        case "heatmap" => toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress))
        case "distance" => toDistanceSeries(YearlyProgress.aggregate(yearlyProgress))
        case "elevation" => toElevationSeries(YearlyProgress.aggregate(yearlyProgress))
        case other => sys.error(s"not supported action: $other")
      }
      Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries)))
    }

    Future.successful(result.getOrElse(InternalServerError))
  }

  // year to date aggregation
  // def mapped to /api/athletes/statistics/ytd/:action
//  @ApiOperation(value = "List year to date series for the logged in athlete",
//    notes = "Returns the year to date series",
//    responseContainer = "List",
//    response = classOf[highcharts.DailySeries],
//    httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 404, message = "Invalid action"),
//    new ApiResponse(code = 500, message = "Internal error")))
  def ytdStatistics(action: String) = AuthAsyncAction {
    implicit request =>

    val maybeAccount = loggedIn
    val now = LocalDate.now()
    logger.info(s"athlete year to date $now statistics for ${maybeAccount.map(_.displayName)}")

    val result = Try {
      val storage = connectivity.getStorage
      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
      val ytdProgress = yearlyProgress.map(_.ytd(now)).map(ytd =>
        YearlyProgress(ytd.year, Seq(DailyProgress(LocalDate.parse(s"${ytd.year}-01-01"), ytd.progress.map(_.progress).foldLeft(Progress.zero)(_ + _)))))

      val dataSeries = action.toLowerCase match {
        case "distance" => toDistanceSeries(ytdProgress)
        case "elevation" => toElevationSeries(ytdProgress)
        case other => sys.error(s"not supported action: $other")
      }
      Ok(Json.obj("status" ->"OK", "series" -> Json.toJson(dataSeries)))
    }

    Future.successful(result.getOrElse(InternalServerError))
  }

  // suggestions when searching
  // def mapped to /api/activities/suggest
//  @ApiOperation(value = "Suggests a list of activities based on the query parameter",
//    notes = "Returns a list of activities",
//    httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 500, message = "Internal error")))
  def suggest(query: String) = timed(s"suggest for $query") { AuthAsyncAction {
    implicit request =>

    logger.debug(s"suggesting for $query")

    // FIXME: workaround until elastic access
    val activities = connectivity.getStorage match {
      // implemented only in OrientDb instance and suggest for logged in user only
      case orientDb: OrientDbStorage => loggedIn.map(account => orientDb.suggestActivities(query, account.athleteId, 10)).getOrElse(List.empty)
      case _ => List.empty
    }
    logger.debug(s"found ${activities.size} activities ...")

    val jsonSuggestions = activities.map{ a =>
      Json.obj("value" -> a.name, "data" -> JsonIo.write(a))
    }
    Future.successful(Ok(
      Json.obj("suggestions" -> jsonSuggestions)
    ))
  }}

  // retrieves the activity with the given id
  // def mapped to /api/activities/:id
//  @ApiOperation(value = "Retrieves an activity",
//    notes = "Returns an activity based on id",
//    httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 403, message = "Forbidden"),
//    new ApiResponse(code = 404, message = "Not found"),
//    new ApiResponse(code = 500, message = "Internal error")))
  def activity(id: Int) = timed(s"query for activity $id") { AuthAsyncAction {
    implicit request =>

      val result = loggedIn.map{ _ =>
        logger.debug(s"querying activity $id")
        connectivity.getStorage.getActivity(id)
          .map(JsonIo.write(_))
          .map(Ok(_))
          .getOrElse(NotFound)
      }.getOrElse(Forbidden)

      Future.successful(result)
  }}

  // retrieves the weather forecast for a given place
  // def mapped to /api/weather/:location
//  @ApiOperation(value = "Retrieves the weather forecast for a specific place",
//    notes = "Weather forecast for the next 5 days",
//    httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 400, message = "Bad request"),
//    new ApiResponse(code = 404, message = "Not found"),
//    new ApiResponse(code = 500, message = "Internal error")))
  def weather(location: String)= timed(s"query weather forecast for $location") { AuthAsyncAction {
    implicit request =>

      // convert city[,country] to city[,isoCountry]
      val isoLocation = CountryIsoUtils.iso(location)
      logger.debug(s"collecting weather forecast for [$location] -> [$isoLocation]")

      // TODO: inject time iterator
      // TODO: make refresh param configurable
      // check location last timestamp and throttle the request
      if (isoLocation.nonEmpty) {
        val now = DateTime.now()
        val maybeLastUpdate = connectivity
          .getStorage
          .getAttribute(isoLocation, "location")
          .map(DateTime.parse(_, DateTimeFormat.forPattern(DateTimePattern.longFormat)))
        val lastUpdate = maybeLastUpdate.getOrElse(now.minusYears(1))
        val elapsedInMinutes = new Duration(lastUpdate, now).getStandardMinutes // should be more than configurable mins to execute the query
        logger.info(s"last weather update on $isoLocation was at $maybeLastUpdate, $elapsedInMinutes minutes ago")
        val forecastEntriesF = if (elapsedInMinutes > 15) {
          logger.info("querying latest weather forecast")
          for {
            entries <- connectivity.getWeatherFeed.query(isoLocation).map(res => res.list.map(w => WeatherForecast(isoLocation, w.dt.getMillis, w)))
            _ <- Future(connectivity.getStorage.storeWeather(entries))
            _ <- Future(connectivity.getStorage.storeAttribute(isoLocation,"location", now.toString(DateTimePattern.longFormat)))
          } yield entries
        } else {
          Future(connectivity.getStorage.listRecentForecast(isoLocation))
        }
        // after a successful query , save the location on the client side (even if the user is not authenticated)
        forecastEntriesF
          .map(DailyWeather.list)
          .map(JsonIo.write(_))
          .map(Ok(_).withCookies(WeatherCookie.create(location)))
          .recover{case _ => NotFound}
      } else {
        Future.successful(BadRequest)
      }
  }}

  // WebSocket to update the client
  // try with https://www.websocket.org/echo.html => ws://localhost:9000/ws
//  @ApiOperation(value = "Initiates a websocket connection",
//    httpMethod = "GET")
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
