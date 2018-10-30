package controllers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import controllers.auth.AuthChecker
import highcharts._
import io.swagger.annotations._
import javax.inject.Inject
import org.joda.time.LocalDate
import org.reactivestreams.{Publisher, Subscriber}
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.model._
import velocorner.storage.OrientDbStorage
import velocorner.util.{JsonIo, Metrics}

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by levi on 06/10/16.
 */
@Api(value = "statistics", protocols = "http")
class ApiController @Inject()(val cache: SyncCacheApi, val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with OriginChecker with Metrics {

  val allowedHosts: Seq[String] = connectivity.allowedHosts

  // def mapped to /api/athletes/progress
  // current year's progress
  @ApiOperation(value = "List the current year's statistics for the logged in athlete",
    notes = "Returns the yearly statistics",
    response = classOf[Progress],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal error")))
  def statistics = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"athlete statistics for ${maybeAccount.map(_.displayName)}")

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
  @ApiOperation(value = "List yearly series for the logged in athlete",
    notes = "Returns the yearly series",
    responseContainer = "List",
    response = classOf[highcharts.DailySeries],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Invalid action"),
    new ApiResponse(code = 500, message = "Internal error")))
  def yearlyStatistics(@ApiParam(value = "heatmap, distance or elevation to fetch", allowableValues = "heatmap, distance, elevation")
             action: String) = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"athlete yearly statistics for ${maybeAccount.map(_.displayName)}")

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
  @ApiOperation(value = "List year to date series for the logged in athlete",
    notes = "Returns the year to date series",
    responseContainer = "List",
    response = classOf[highcharts.DailySeries],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Invalid action"),
    new ApiResponse(code = 500, message = "Internal error")))
  def ytdStatistics(@ApiParam(value = "distance or elevation to fetch", allowableValues = "distance, elevation")
          action: String) = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    val now = LocalDate.now()
    Logger.info(s"athlete year to date $now statistics for ${maybeAccount.map(_.displayName)}")

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
  @ApiOperation(value = "Suggests a list of activities based on the query parameter",
    notes = "Returns a list of activities",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal error")))
  def suggest(@ApiParam(value = "partial input matched for activities")
              query: String) = timed(s"suggest for $query") { AuthAsyncAction { implicit request =>
    Logger.debug(s"suggesting for $query")

    // FIXME: workaround until elastic access
    val activities = connectivity.getStorage match {
      // implemented only in OrientDb instance and suggest for logged in user only
      case orientDb: OrientDbStorage => loggedIn.map(account => orientDb.suggestActivities(query, account.athleteId, 10)).getOrElse(List.empty)
      case _ => List.empty
    }
    Logger.debug(s"found ${activities.size} activities ...")

    val jsonSuggestions = activities.map{ a =>
      Json.obj("value" -> a.name, "data" -> JsonIo.write(a))
    }
    Future.successful(Ok(
      Json.obj("suggestions" -> jsonSuggestions)
    ))
  }}

  // retrieves the activity with the given id
  // def mapped to /api/activities/:id
  @ApiOperation(value = "Retrieves an activity",
    notes = "Returns an activity based on id",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 403, message = "Forbidden"),
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 500, message = "Internal error")))
  def activity(@ApiParam(value = "identifier of the activity")
              id: Int) = timed(s"query for acticity $id") { AuthAsyncAction { implicit request =>

    val result = loggedIn.map{account =>
      Logger.debug(s"querying activity $id")
      connectivity.getStorage.getActivity(id)
        .map(a => Ok(JsonIo.write(a)))
        .getOrElse(NotFound)
    }.getOrElse(Forbidden)

    Future.successful(result)
  }}

  // WebSocket to update the client
  // try with https://www.websocket.org/echo.html => ws://localhost:9000/ws
  @ApiOperation(value = "Initiates a websocket connection",
    httpMethod = "GET")
  def ws: WebSocket = WebSocket.acceptOrResult[String, String] { rh =>
    rh match {
      case _ if sameOriginCheck(rh) =>
        Logger.info(s"ws with request header: $rh")
        val flow = wsFlow(rh)
        Future.successful[Either[Result, Flow[String, String, _]]](Right(flow)).recover{ case e =>
          Logger.error("failed to create websocket", e)
          Left(InternalServerError(s"failed to create websocket, ${e.getMessage}"))
        }
      case rejected =>
        Logger.error(s"same origin check failed for $rejected")
        Future.successful(Left(Forbidden))
    }
  }

  var counter = 1
  private def wsFlow(rh: RequestHeader): Flow[String, String, NotUsed] = {
    // echo input
    val in = Sink.foreach[String](println)
    //send messages from the publisher and then leave the socket open
    //val out1 = Source.single("Welcome").concat(Source.maybe)
    val out = Source.fromPublisher((s: Subscriber[_ >: String]) => {
      s.onNext(s"hello $counter")
      counter = counter + 1
    })

    Flow.fromSinkAndSource(in, out)
  }
}
