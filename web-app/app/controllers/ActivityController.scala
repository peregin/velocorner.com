package controllers

import controllers.auth.AuthChecker
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.{Achievements, Progress}
import velocorner.model._
import velocorner.storage.{OrientDbStorage, PsqlDbStorage}
import velocorner.util.{JsonIo, Metrics}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.implicits._
import cats.data.{EitherT, OptionT}
import model.{apexcharts, highcharts}
import velocorner.api.strava.Activity


class ActivityController @Inject()(val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with Metrics {

  // def mapped to /api/athletes/statistics/profile/:activity
  // current year's progress
  def ytdProfile(activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for profile in $activity") { implicit request =>

      val storage = connectivity.getStorage
      val now = LocalDate.now()
      val currentYear = now.getYear

      def yearlyProgress(activities: Iterable[Activity]): Progress = {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)
        val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
        aggregatedYearlyProgress.headOption.map(_.progress.last.progress).getOrElse(Progress.zero)
      }

      val statisticsOT = for {
        account <- OptionT(Future(loggedIn))
        _ = logger.info(s"athletes' $activity statistics for ${account.displayName}")
        activities <- OptionT.liftF(storage.listAllActivities(account.athleteId, activity))
        _ = logger.debug(s"found ${activities.size} activities for ${account.athleteId}")
        ytdActivities = activities.filter(_.getStartDateLocal().toLocalDate.getYear == currentYear)
        ytdCommutes = ytdActivities.filter(_.commute.getOrElse(false))
      } yield ProfileStatistics.from(now, yearlyProgress(ytdActivities), yearlyProgress(ytdCommutes))

      statisticsOT
        .getOrElse(ProfileStatistics.zero)
        .map(Json.toJson(_))
        .map(Ok(_))
    }

  // route mapped to /api/athletes/statistics/yearly/:action/:activity
  def yearlyStatistics(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for yearly statistics in $action/$activity") { implicit request =>

      val storage = connectivity.getStorage

      val result = for {
        account <- OptionT(Future(loggedIn))
        _ = logger.info(s"athlete yearly statistics for ${account.displayName}")
        activities <- OptionT.liftF(timedFuture(s"storage list all for $action/$activity")(storage.listAllActivities(account.athleteId, activity)))
        dailyProgress = DailyProgress.from(activities)
        yearlyProgress = YearlyProgress.from(dailyProgress)
      } yield yearlyProgress

      result
        .getOrElse(Iterable.empty)
        .map { yearlyProgress =>
          action.toLowerCase match {
            case "heatmap" => highcharts.toDistanceSeries(YearlyProgress.zeroOnMissingDate(yearlyProgress))
            case "distance" => highcharts.toDistanceSeries(YearlyProgress.aggregate(yearlyProgress))
            case "elevation" => highcharts.toElevationSeries(YearlyProgress.aggregate(yearlyProgress))
            case other => sys.error(s"not supported action: $other")
          }
        }
        .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
    }

  // year to date aggregation
  // route mapped to /api/athletes/statistics/ytd/:action/:activity
  def ytdStatistics(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for ytd statistics in $action/$activity") { implicit request =>

      val now = LocalDate.now()
      val storage = connectivity.getStorage

      val result = for {
        account <- OptionT(Future(loggedIn))
        _ = logger.info(s"athlete year to date $now statistics for ${account.displayName}")
        activities <- OptionT.liftF(storage.listAllActivities(account.athleteId, activity))
        dailyProgress = DailyProgress.from(activities)
        yearlyProgress = YearlyProgress.from(dailyProgress)
        ytdProgress = yearlyProgress.map(_.ytd(now)).map(ytd =>
          YearlyProgress(ytd.year, Seq(
            DailyProgress(LocalDate.parse(s"${ytd.year}-01-01"), ytd.progress.map(_.progress).foldLeft(Progress.zero)(_ + _)))
          ))
      } yield ytdProgress

      result
        .getOrElse(Iterable.empty)
        .map { ytdProgress =>
          action.toLowerCase match {
            case "distance" => highcharts.toDistanceSeries(ytdProgress)
            case "elevation" => highcharts.toElevationSeries(ytdProgress)
            case other => sys.error(s"not supported action: $other")
          }
        }
        .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
    }

  // yearly histogram based on predefined ranges
  // route mapped to /api/athletes/statistics/histogram/:action/:activity
  def yearlyHistogram(action: String, activity: String): Action[AnyContent] = {
    TimedAuthAsyncAction(s"query for heatmap statistics in $action/$activity") { implicit request =>

      val storage = connectivity.getStorage
      val result = for {
        account <- OptionT(Future(loggedIn))
        activities <- OptionT.liftF(storage.listAllActivities(account.athleteId, activity))
        series = action.toLowerCase match {
          case "distance" => apexcharts.toDistanceHeatmap(activities, activity)
          case "elevation" => apexcharts.toElevationHeatmap(activities)
          case other => sys.error(s"not supported action: $other")
        }
      } yield series

      result.getOrElse(List.empty).map(series => Json.toJson(series)).map(Ok(_))
    }
  }

  // list of achievements
  // route mapped to /api/statistics/achievements/:activity
  def achievements(activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for achievements in $activity") { implicit request =>

      val storage = connectivity.getStorage.getAchievementStorage
      loggedIn.map { account =>
        // parallelization
        val maxAverageSpeedF = storage.maxAverageSpeed(account.athleteId, activity)
        val maxDistanceF = storage.maxDistance(account.athleteId, activity)
        val maxElevationF = storage.maxElevation(account.athleteId, activity)
        val maxAveragePowerF = storage.maxAveragePower(account.athleteId, activity)
        val maxHeartRateF = storage.maxHeartRate(account.athleteId, activity)
        val maxAverageHeartRateF = storage.maxAverageHeartRate(account.athleteId, activity)
        val minAverageTemperatureF = storage.minAverageTemperature(account.athleteId, activity)
        val maxAverageTemperatureF = storage.maxAverageTemperature(account.athleteId, activity)
        val achievements = for {
          maxAverageSpeed <- maxAverageSpeedF
          maxDistance <- maxDistanceF
          maxElevation <- maxElevationF
          maxAveragePower <- maxAveragePowerF
          maxHeartRate <- maxHeartRateF
          maxAverageHeartRate <- maxAverageHeartRateF
          minTemperature <- minAverageTemperatureF
          maxTemperature <- maxAverageTemperatureF
        } yield Achievements(
          maxAverageSpeed = maxAverageSpeed,
          maxDistance = maxDistance,
          maxElevation = maxElevation,
          maxAveragePower = maxAveragePower,
          maxHeartRate = maxHeartRate,
          maxAverageHeartRate = maxAverageHeartRate,
          minAverageTemperature = minTemperature,
          maxAverageTemperature = maxTemperature
        )
        achievements.map(JsonIo.write[Achievements](_)).map(Ok(_))
      }.getOrElse(Future(Unauthorized))
    }

  // suggestions when searching, workaround until elastic access, use the storage directly
  // route mapped to /api/activities/suggest
  def suggest(query: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"suggest for $query") { implicit request =>

      logger.debug(s"suggesting for $query")
      val storage = connectivity.getStorage
      val suggestionsTF = for {
        account <- OptionT(Future(loggedIn))
        suggestions <- OptionT.liftF(
          storage match {
            case orientDb: OrientDbStorage => orientDb.suggestActivities(query, account.athleteId, 10)
            case psqlDb: PsqlDbStorage => psqlDb.suggestActivities(query, account.athleteId, 10)
            case other =>
              logger.warn(s"$other is not supporting suggestions")
              Future(Iterable.empty)
          }
        )
      } yield suggestions

      suggestionsTF
        .getOrElse(Iterable.empty)
        .map { activities =>
          logger.debug(s"found ${activities.size} suggested activities ...")
          activities.map(a => Json.obj("value" -> a.name, "data" -> JsonIo.write(a)))
        }
        .map(jsonSuggestions => Ok(Json.obj("suggestions" -> jsonSuggestions)))
    }

  // retrieves the activity with the given id
  // route mapped to /api/activities/:id
  def activity(id: Long): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for activity $id") { implicit request =>

      val resultET = for {
        _ <- EitherT(Future(loggedIn.toRight(Forbidden)))
        activity <- EitherT(connectivity.getStorage.getActivity(id).map(_.toRight(NotFound)))
      } yield activity

      resultET
        .map(JsonIo.write(_))
        .map(Ok(_))
        .merge
    }

  // route mapped to /api/activities/type
  def activityTypes: Action[AnyContent] = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage
    val resultTF = for {
      account <- OptionT(Future(loggedIn))
      types <- OptionT.liftF(storage.listActivityTypes(account.athleteId))
      _ = logger.debug(s"account ${account.displayName} did ${types.mkString(",")}")
    } yield types

    resultTF
      .map(ts => Ok(JsonIo.write(ts)))
      .getOrElse(NotFound)
  }
}
