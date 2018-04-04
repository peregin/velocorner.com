package controllers

import javax.inject.Inject
import akka.stream.scaladsl.{Flow, Sink, Source}
import controllers.auth.AuthChecker
import highcharts._
import io.swagger.annotations._
import org.joda.time.LocalDate
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import velocorner.model._

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by levi on 06/10/16.
  */
@Api(value = "statistics", protocols = "http")
class RestController @Inject()(val cache: SyncCacheApi, val connectivity: ConnectivitySettings, strategy: RefreshStrategy, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with OriginChecker {

  // mapped to /rest/club/:action
  @ApiOperation(value = "List daily club activities",
    notes = "Returns daily aggregated activities submitted recently",
    responseContainer = "List",
    response = classOf[highcharts.DailySeries],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Invalid action"),
    new ApiResponse(code = 500, message = "Internal error")))
  def recentClub(@ApiParam(value = "distance or elevation to fetch", allowableValues = "distance, elevation")
                 action: String) = Action.async { implicit request =>
    Logger.info(s"recent club action for $action")

    val result = Try {
      // sync, load if needed
      strategy.refreshClubActivities(Club.Velocorner)

      val storage = connectivity.getStorage
      val dailyAthleteProgress = storage.dailyProgressForAll(200)
      val mostRecentAthleteProgress = AthleteDailyProgress.keepMostRecentDays(dailyAthleteProgress, 14)

      val clubAthleteIds = storage.getClub(Club.Velocorner).map(_.memberIds).getOrElse(List.empty)
      val clubAthletes = clubAthleteIds.flatMap(id => storage.getAthlete(id))
      val id2Members = clubAthletes.map(a => (a.id.toString, a.firstname.getOrElse(a.id.toString))).toMap
      val seriesId2Name = (ds: DailySeries) => ds.copy(name = id2Members.getOrElse(ds.name, ds.name))

      val dataSeries = action.toLowerCase match {
        case "distance" => toAthleteDistanceSeries(mostRecentAthleteProgress)
        case "elevation" => toAthleteElevationSeries(mostRecentAthleteProgress)
        case other => sys.error(s"not supported action: $action")
      }
      val series = dataSeries.map(_.aggregate).map(seriesId2Name)
      Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(series)))
    }.recover{ case a if a.getMessage.startsWith("not supported") =>
      Logger.error("failed to retrieve daily club activities", a)
      NotFound
    }

    Future.successful(result.getOrElse(InternalServerError))
  }

  // def mapped to /rest/athlete/progress
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

  // def mapped to /rest/athlete/yearly/:action
  @ApiOperation(value = "List yearly series for the logged in athlete",
    notes = "Returns the yearly series",
    responseContainer = "List",
    response = classOf[highcharts.DailySeries],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Invalid action"),
    new ApiResponse(code = 500, message = "Internal error")))
  def yearly(@ApiParam(value = "heatmap, distance or elevation to fetch", allowableValues = "heatmap, distance, elevation")
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
  // def mapped to /rest/athlete/ytd/:action
  @ApiOperation(value = "List year to date series for the logged in athlete",
    notes = "Returns the year to date series",
    responseContainer = "List",
    response = classOf[highcharts.DailySeries],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Invalid action"),
    new ApiResponse(code = 500, message = "Internal error")))
  def ytd(@ApiParam(value = "distance or elevation to fetch", allowableValues = "distance, elevation")
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

  // suggestions when searching, mapped to /rest/suggest
  def suggest(query: String) = AuthAsyncAction { implicit request =>
    Logger.debug(s"suggesting for $query")
    Future.successful(Ok(Json.arr(JsString("Hungary"))))
  }

  def ws: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Logger.info(s"websocket with request: $request")
    // origin checker?

    // Log events to the console
    val in = Sink.foreach[String](println)

    // Send a single 'Hello!' message and then leave the socket open
    val out = Source.single("Hello!").concat(Source.maybe)

    val flow = Flow.fromSinkAndSource(in, out)

    //Future.successful(Left(NotImplemented))
    Future.successful(Right(flow))
  }
}
