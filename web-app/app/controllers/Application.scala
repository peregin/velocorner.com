package controllers


import controllers.auth.AuthConfigSupport
import highcharts.DailySeries
import jp.t2v.lab.play2.auth.{Logout, OptionalAuthElement}
import org.joda.time.{DateTime, LocalDate}
import org.slf4s
import play.Logger
import play.api.mvc._
import velocorner.model._
import velocorner.proxy.StravaFeed
import velocorner.util.Metrics

import scala.annotation.tailrec
import scala.concurrent.Future

object Application extends Controller with OptionalAuthElement with AuthConfigSupport with Logout with Metrics {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val log = new slf4s.Logger(Logger.underlying())

  @volatile var lastClubUpdateTs = 0L

  def index = StackAction{ implicit request =>
    Logger.info("rendering landing page...")

    val context = timed("building page context") {
      val maybeAccount = loggedIn
      Logger.info(s"rendering for $maybeAccount")
      val storage = Global.getStorage

      val nowInMillis = DateTime.now().getMillis
      val diffInMillis = nowInMillis - lastClubUpdateTs
      lastClubUpdateTs = nowInMillis
      if (diffInMillis > 1200000) {
        Logger.info("refreshing club information from Stava")
        // update from Strava
        val feed = Global.getFeed
        val clubActivities = feed.listRecentClubActivities(Club.Velocorner)
        storage.store(clubActivities)
        val clubAthletes = feed.listClubAthletes(Club.Velocorner)
        clubAthletes.foreach(storage.store)
        val club = Club(Club.Velocorner, clubAthletes.map(_.id))
        storage.store(club)
      }

      val currentYear = LocalDate.now().getYear
      val yearlyProgress = maybeAccount.map(account => YearlyProgress.from(storage.dailyProgressForAthlete(account.athleteId))).getOrElse(Iterable.empty)
      val flattenedYearlyProgress = YearlyProgress.zeroOnMissingDate(yearlyProgress)
      val aggregatedYearlyProgress = YearlyProgress.aggregate(yearlyProgress)
      val currentYearStatistics = aggregatedYearlyProgress.find(_.year == currentYear).map(_.progress.last.progress).getOrElse(Progress.zero)

      val dailyAthleteProgress = storage.dailyProgressForAll(200)
      val mostRecentAthleteProgress = AthleteDailyProgress.keepMostRecentDays(dailyAthleteProgress, 14)

      val clubAthleteIds = storage.getClub(Club.Velocorner).map(_.memberIds).getOrElse(List.empty)
      val clubAthletes = clubAthleteIds.flatMap(id => storage.getAthlete(id))
      val id2Members = clubAthletes.map(a => (a.id.toString, a.firstname.getOrElse(a.id.toString))).toMap
      val seriesId2Name = (ds: DailySeries) => ds.copy(name = id2Members.getOrElse(ds.name, ds.name))

      import highcharts._

      LandingPageContext(
        maybeAccount,
        currentYearStatistics,
        toDistanceSeries(flattenedYearlyProgress),
        toDistanceSeries(aggregatedYearlyProgress),
        toAthleteDistanceSeries(mostRecentAthleteProgress).map(_.aggregate).map(seriesId2Name),
        toAthleteElevationSeries(mostRecentAthleteProgress).map(_.aggregate).map(seriesId2Name)
      )
    }

    Ok(views.html.index(context))
  }

  def refresh = AsyncStack{ implicit request =>
    val maybeAccount = loggedIn
    Logger.info(s"refreshing for $maybeAccount")
    maybeAccount.foreach{ account =>
      // allow refresh after some time only
      val now = DateTime.now()
      val lastUpdate = account.lastUpdate.getOrElse(now.minusYears(1)) // if not set, then consider it as very old
      val diffInMillis = now.getMillis - lastUpdate.getMillis
      if (diffInMillis > 60000) {
        log.info(s"last update was $diffInMillis millis ago...")
        val storage = Global.getStorage
        val feed = Global.getFeed(account.accessToken)

        val lastActivitiyIds = storage.listRecentActivities(account.athleteId, StravaFeed.maxItemsPerPage).map(_.id).toSet

        @tailrec
        def list(page: Int, accu: Iterable[Activity]): Iterable[Activity] = {
          val activities = feed.listAthleteActivities(page, StravaFeed.maxItemsPerPage)
          val activityIds = activities.map(_.id).toSet
          if (activities.size < StravaFeed.maxItemsPerPage || activityIds.intersect(lastActivitiyIds).nonEmpty) activities.filter(a => !lastActivitiyIds.contains(a.id)) ++ accu
          else list(page + 1, activities ++ accu)
        }
        val newActivities = list(1, List.empty)
        log.info(s"found ${newActivities.size} new activities")
        storage.store(newActivities)
        storage.store(account.copy(lastUpdate = Some(now)))
      }
    }
    Future.successful(Redirect(routes.Application.index()))
  }

  def logout = Action.async{ implicit request =>
    gotoLogoutSucceeded
  }

  def about = Action {
    Ok(views.html.about())
  }
}