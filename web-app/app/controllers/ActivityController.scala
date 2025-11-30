package controllers

import cats.data.{EitherT, OptionT}
import cats.implicits._
import controllers.auth.AuthChecker
import controllers.util.ActivityOps
import model.apexcharts
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import velocorner.api.chart.DailyPoint
import velocorner.api.strava.Activity
import velocorner.api.wordcloud.WordCloud
import velocorner.api.{Account, Achievements, ProfileStatistics, Progress, Units}
import velocorner.model._
import velocorner.util.{JsonIo, Metrics}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ActivityController @Inject() (val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
    extends AbstractController(components)
    with ActivityOps
    with AuthChecker
    with Metrics {

  // def mapped to /api/athletes/statistics/profile/:activity
  // current year's progress
  def profile(activity: String, year: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for profile in $activity")(parse.default) { implicit request =>
      val storage = connectivity.getStorage
      val ytd = LocalDate.now().withYear(year.toInt)
      logger.info(s"profile from $ytd for $activity")

      def yearlyProgress(activities: Iterable[Activity], unit: Units.Entry): Progress = {
        val dailyProgress = DailyProgress.from(activities)
        val yearlyProgress = YearlyProgress.from(dailyProgress)
        val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
        val progress = aggregatedYearlyProgress.headOption.map(_.progress.last.progress).getOrElse(Progress.zero)
        progress.to(unit)
      }

      val statisticsOT = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        _ = logger.info(s"athletes' $activity profile for ${account.displayName}")
        activities <- OptionT.liftF(storage.listYtdActivities(account.athleteId, activity, year.toInt))
        ytdActivities = activities.filter(_.getStartDateLocal().toLocalDate.compareTo(ytd) <= 0)
        _ = logger.debug(s"found ${ytdActivities.size} activities in $year for ${account.athleteId}")
        ytdCommutes = ytdActivities.filter(_.commute.getOrElse(false))
      } yield ProfileStatistics.from(
        ytd,
        yearlyProgress(ytdActivities, account.units()),
        yearlyProgress(ytdCommutes, account.units())
      )

      statisticsOT
        .getOrElse(ProfileStatistics.zero)
        .map(Json.toJson(_))
        .map(Ok(_))
    }

  // route mapped to /api/athletes/statistics/yearly/:action/:activity
  def yearlyStatistics(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for yearly statistics in $action/$activity")(parse.default) { implicit request =>
      val storage = connectivity.getStorage

      val result = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        _ = logger.info(s"athlete yearly statistics for ${account.displayName}")
        activities <- OptionT.liftF(
          timedFuture(s"storage list all for $action/$activity")(storage.listAllActivities(account.athleteId, activity))
        )
        series = toYearlySeries(activities, action, account.units())
      } yield series

      result
        .getOrElse(Iterable.empty)
        .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
    }

  // year to date aggregation
  // route mapped to /api/athletes/statistics/ytd/:action/:activity
  def ytdStatistics(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for ytd statistics in $action/$activity")(parse.default) { implicit request =>
      val now = LocalDate.now()
      val storage = connectivity.getStorage
      val result = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        units = account.units()
        _ = logger.info(s"athlete year to date $now statistics for ${account.displayName}")
        activities <- OptionT.liftF(storage.listAllActivities(account.athleteId, activity))
      } yield toSumYtdSeries(activities, now, action, units)

      result
        .getOrElse(Iterable.empty)
        .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
    }

  // all daily activity list for the last 12 months, shown in the calendar heatmap
  // route mapped to /api/athletes/statistics/daily/:action
  def dailyStatistics(action: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for all daily activities in $action")(parse.default) { implicit request =>
      val now = DateTime.now(DateTimeZone.UTC)
      val last12Month = now.minusMonths(12)
      val storage = connectivity.getStorage

      val result = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        _ = logger.info(s"athlete daily statistics from date $last12Month statistics for ${account.displayName}")
        activities <- OptionT.liftF(storage.listActivities(account.athleteId, last12Month, now.plusDays(1)))
        dailyProgress = DailyProgress.from(activities)
        series = action.toLowerCase match {
          case "distance"  => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(account.units()).distance))
          case "elevation" => dailyProgress.map(dp => DailyPoint(dp.day, dp.progress.to(account.units()).elevation))
          case other       => sys.error(s"not supported action: $other")
        }
      } yield series

      result
        .getOrElse(Iterable.empty)
        .map(series => Ok(Json.toJson(series)))
    }

  // yearly histogram based on predefined ranges
  // route mapped to /api/athletes/statistics/histogram/:action/:activity
  def yearlyHistogram(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for heatmap statistics in $action/$activity")(parse.default) { implicit request =>
      val result = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        storage = connectivity.getStorage
        activities <- OptionT.liftF(storage.listAllActivities(account.athleteId, activity))
        series = action.toLowerCase match {
          case "distance"  => apexcharts.toDistanceHeatmap(activities, activity, account.units())
          case "elevation" => apexcharts.toElevationHeatmap(activities, account.units())
          case other       => sys.error(s"not supported action: $other")
        }
      } yield series

      result.getOrElse(List.empty).map(series => Json.toJson(series)).map(Ok(_))
    }

  // top 10 activities for distance and elevation
  // route mapped to /api/athletes/statistics/top/:action/:activity
  def top(action: String, activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"top 10 statistics in $action/$activity")(parse.default) { implicit request =>
      val result = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        storage = connectivity.getStorage
        activities <- OptionT.liftF(storage.listTopActivities(account.athleteId, ActionType(action), activity, 10))
      } yield activities
      result.getOrElse(List.empty).map(Json.toJson(_)).map(Ok(_))
    }

  // list of achievements
  // route mapped to /api/statistics/achievements/:activity
  def achievements(activity: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for achievements in $activity")(parse.default) { implicit request =>
      val storage = connectivity.getStorage.getAchievementStorage
      loggedIn
        .map { account =>
          // parallelization
          val maxAverageSpeedF = storage.maxAverageSpeed(account.athleteId, activity)
          val maxDistanceF = storage.maxDistance(account.athleteId, activity)
          val maxTimeF = storage.maxTime(account.athleteId, activity)
          val maxElevationF = storage.maxElevation(account.athleteId, activity)
          val maxAveragePowerF = storage.maxAveragePower(account.athleteId, activity)
          val maxHeartRateF = storage.maxHeartRate(account.athleteId, activity)
          val maxAverageHeartRateF = storage.maxAverageHeartRate(account.athleteId, activity)
          val minAverageTemperatureF = storage.minAverageTemperature(account.athleteId, activity)
          val maxAverageTemperatureF = storage.maxAverageTemperature(account.athleteId, activity)
          val achievements = for {
            maxAverageSpeed <- maxAverageSpeedF
            maxDistance <- maxDistanceF
            maxTime <- maxTimeF
            maxElevation <- maxElevationF
            maxAveragePower <- maxAveragePowerF
            maxHeartRate <- maxHeartRateF
            maxAverageHeartRate <- maxAverageHeartRateF
            minTemperature <- minAverageTemperatureF
            maxTemperature <- maxAverageTemperatureF
          } yield Achievements(
            maxAverageSpeed = maxAverageSpeed,
            maxDistance = maxDistance,
            maxTimeInSec = maxTime,
            maxElevation = maxElevation,
            maxAveragePower = maxAveragePower,
            maxHeartRate = maxHeartRate,
            maxAverageHeartRate = maxAverageHeartRate,
            minAverageTemperature = minTemperature,
            maxAverageTemperature = maxTemperature
          ).to(account.units())
          achievements.map(JsonIo.write[Achievements](_)).map(Ok(_))
        }
        .getOrElse(Future(Unauthorized))
    }

  // retrieves the activity with the given id
  // route mapped to /api/activities/last
  def last(): Action[AnyContent] =
    TimedAuthAsyncAction("query for last activity")(parse.default) { implicit request =>
      val resultET = for {
        account <- EitherT[Future, Status, Account](Future(loggedIn.toRight(Forbidden)))
        activity <- EitherT[Future, Status, Activity](connectivity.getStorage.getLastActivity(account.athleteId).map(_.toRight(NotFound)))
      } yield activity

      resultET
        .map(JsonIo.write(_))
        .map(Ok(_))
        .merge
    }

  // retrieves the activity with the given id
  // route mapped to /api/activities/:id
  def activity(id: Long): Action[AnyContent] =
    TimedAuthAsyncAction(s"query for activity $id")(parse.default) { implicit request =>
      val resultET = for {
        _ <- EitherT[Future, Status, Account](Future(loggedIn.toRight(Forbidden)))
        activity <- EitherT[Future, Status, Activity](connectivity.getStorage.getActivity(id).map(_.toRight(NotFound)))
      } yield activity

      resultET
        .map(JsonIo.write(_))
        .map(Ok(_))
        .merge
    }

  // route mapped to /api/activities/types
  def activityTypes: Action[AnyContent] = AuthAsyncAction(parse.default) { implicit request =>
    val storage = connectivity.getStorage
    val resultTF = for {
      account <- OptionT[Future, Account](Future(loggedIn))
      types <- OptionT.liftF(storage.listActivityTypes(account.athleteId))
      _ = logger.debug(s"account ${account.displayName} did ${types.mkString(",")}")
    } yield types

    resultTF
      .map(ts => Ok(JsonIo.write(ts)))
      .getOrElse(Forbidden)
  }

  // route mapped to /api/activities/:activity/years
  def activityYears(activity: String): Action[AnyContent] = AuthAsyncAction(parse.default) { implicit request =>
    val storage = connectivity.getStorage
    val resultTF = for {
      account <- OptionT[Future, Account](Future(loggedIn))
      years <- OptionT.liftF(storage.listActivityYears(account.athleteId, activity))
      _ = logger.debug(s"account ${account.displayName} for $activity has activities in ${years.mkString(",")}")
    } yield years

    resultTF
      .map(ts => Ok(JsonIo.write(ts)))
      .getOrElse(Forbidden)
  }

  // suggestions when searching, workaround until elastic access, use the storage directly
  // route mapped to /api/activities/suggest
  def suggest(query: String): Action[AnyContent] =
    TimedAuthAsyncAction(s"suggest for $query")(parse.default) { implicit request =>
      logger.debug(s"suggesting for $query")
      val storage = connectivity.getStorage
      val suggestionsTF = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        suggestions <- OptionT.liftF(storage.suggestActivities(query, account.athleteId, 10))
      } yield suggestions

      suggestionsTF
        .getOrElse(Iterable.empty)
        .map { activities =>
          logger.debug(s"found ${activities.size} suggested activities ...")
          activities.map(a => Json.obj("value" -> a.name, "data" -> JsonIo.write(a)))
        }
        .map(jsonSuggestions => Ok(Json.obj("suggestions" -> jsonSuggestions)))
    }

  // word cloud titles and descriptions with occurrences
  // route mapped to /api/activities/wordcloud
  def wordcloud(): Action[AnyContent] =
    TimedAuthAsyncAction("wordcloud")(parse.default) { implicit request =>
      val storage = connectivity.getStorage
      val wordsTF = for {
        account <- OptionT[Future, Account](Future(loggedIn))
        words <- OptionT.liftF(storage.activityTitles(account.athleteId, 400))
        series = words
          .map(_.replace("#commutemarker.com", ""))
          .map(_.trim.toLowerCase)
          .groupBy(identity)
          .view
          .mapValues(_.size)
          .map { case (k, v) => WordCloud(k, v) }
          .toList
          .filter(_.weight > 1)
          .sortBy(_.weight)
          .reverse
      } yield series
      wordsTF
        .getOrElse(Iterable.empty)
        .map(series => Ok(JsonIo.write(series)))
    }
}
