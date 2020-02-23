package controllers

import controllers.auth.AuthChecker
import highcharts._
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import scalaz.Scalaz._
import scalaz.{OptionT, _}
import velocorner.api.{Achievements, Progress}
import velocorner.model._
import velocorner.storage.OrientDbStorage
import velocorner.util.{JsonIo, Metrics}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ActivityController @Inject()(val connectivity: ConnectivitySettings, val cache: SyncCacheApi, components: ControllerComponents)
  extends AbstractController(components) with AuthChecker with Metrics {

  // def mapped to /api/athletes/statistics/profile/:activity
  // current year's progress
  def ytdProfile(activity: String) = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage
    val now = LocalDate.now()
    val currentYear = now.getYear

    val statisticsOT = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athletes' $activity statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId, activity).liftM[OptionT]
      yearlyProgress = YearlyProgress.from(dailyProgress)
      aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      currentYearProgress = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)
    } yield ProfileStatistics.from(now, currentYearProgress)

    statisticsOT
      .getOrElse(ProfileStatistics.zero)
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  // route mapped to /api/athletes/statistics/yearly/:action/:activity
  def yearlyStatistics(action: String, activity: String) = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage

    val result = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athlete yearly statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId, activity).liftM[OptionT]
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
      .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
  }

  // year to date aggregation
  // route mapped to /api/athletes/statistics/ytd/:action/:activity
  def ytdStatistics(action: String, activity: String) = AuthAsyncAction { implicit request =>
    val now = LocalDate.now()
    val storage = connectivity.getStorage

    val result = for {
      account <- OptionT(Future(loggedIn))
      _ = logger.info(s"athlete year to date $now statistics for ${account.displayName}")
      dailyProgress <- storage.dailyProgressForAthlete(account.athleteId, activity).liftM[OptionT]
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
      .map(dataSeries => Ok(Json.obj("status" -> "OK", "series" -> Json.toJson(dataSeries))))
  }

  // list of achievements
  // route mapped to /api/statistics/achievements/:activity
  def achievements(activity: String) = AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage.getAchievementStorage
    loggedIn.map{ account =>
      // parallelization
      val maxAverageSpeedF = storage.maxAverageSpeed(account.athleteId, activity)
      val maxDistanceF = storage.maxDistance(account.athleteId, activity)
      val maxElevationF = storage.maxElevation(account.athleteId, activity)
      val maxAveragePowerF = storage.maxAveragePower(account.athleteId, activity)
      val maxHeartRateF = storage.maxHeartRate(account.athleteId, activity)
      val maxAverageHeartRateF = storage.maxAverageHeartRate(account.athleteId, activity)
      val achievements = for {
        maxAverageSpeed <- maxAverageSpeedF
        maxDistance <- maxDistanceF
        maxElevation <- maxElevationF
        maxAveragePower <- maxAveragePowerF
        maxHeartRate <- maxHeartRateF
        maxAverageHeartRate <- maxAverageHeartRateF
      } yield Achievements(
        maxAverageSpeed = maxAverageSpeed,
        maxDistance = maxDistance,
        maxElevation = maxElevation,
        maxAveragePower = maxAveragePower,
        maxHeartRate = maxHeartRate,
        maxAverageHeartRate = maxAverageHeartRate
      )
      achievements.map(JsonIo.write[Achievements](_)).map(Ok(_))
    }.getOrElse(Future(Unauthorized))
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

  // route mapped to /api/activities/type
  def activityTypes = { AuthAsyncAction { implicit request =>
    val storage = connectivity.getStorage
    val resultTF = for {
      account <- OptionT(Future(loggedIn))
      types <- storage.listActivityTypes(account.athleteId).liftM[OptionT]
      _ = logger.debug(s"account ${account.displayName} did ${types.mkString(",")}")
    } yield types

    resultTF
      .map(ts => Ok(JsonIo.write(ts)))
      .getOrElse(NotFound)
  }}
}
