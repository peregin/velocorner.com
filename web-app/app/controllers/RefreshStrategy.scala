package controllers

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import org.slf4s
import org.slf4s.Logging
import play.Logger
import velocorner.model.Account
import velocorner.feed.{ActivityFeed, StravaActivityFeed}
import velocorner.model.strava.Activity
import velocorner.storage.Storage
import velocorner.util.CloseableResource

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Isolate the update the logic to refresh club and account activities.
  */
@Singleton
class RefreshStrategy @Inject()(connectivity: ConnectivitySettings) extends Logging with CloseableResource {

  override val log = new slf4s.Logger(Logger.underlying())

  // won't refresh from the feed if was updated within this time period
  val stalePeriodInMillis = 60000 // more than a minute

  // query from the storage and eventually from the activity feed
  def refreshAccountActivities(account: Account): Future[Iterable[Activity]] = {
    // allow refresh after some time only
    val now = DateTime.now()
    val storage = connectivity.getStorage
    for {
      newActivities: Iterable[Activity] <- withCloseable(connectivity.getStravaFeed(account.accessToken)) { feed =>
        log.info(s"refresh for athlete: ${account.athleteId}, last update: ${account.lastUpdate}")
        retrieveNewActivities(feed, storage, account.athleteId, account.lastUpdate, now)
      }
      // log the most recent activity
      maybeMostRecent = newActivities.map(_.start_date).toSeq.sortWith((a, b) => a.compareTo(b) > 0).headOption
      _ = log.info(s"most recent activity retrieved is from $maybeMostRecent")
      _ <- storage.storeActivity(newActivities)
      _ <- storage.store(account.copy(lastUpdate = Some(now)))
    } yield newActivities
  }

  def retrieveNewActivities(feed: ActivityFeed, storage: Storage, athleteId: Long, lastUpdate: Option[DateTime], now: DateTime): Future[Iterable[Activity]] = {
    lastUpdate.map(_.getMillis) match {

      case None => // it was never synched, do a full update
        log.info(s"retrieving all activities as it was never synched")
        StravaActivityFeed.listAllAthleteActivities(feed)

      case Some(lastUpdateInMillis) if now.getMillis - lastUpdateInMillis > stalePeriodInMillis =>
        log.info(s"retrieving latest activities for $athleteId")

        def list(page: Int, lastActivityIds: Set[Long], accu: Iterable[Activity]): Future[Iterable[Activity]] = for {
          activities <- feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
          _ = log.debug(s"refresh page $page, activities ${activities.size}")
          activityIds = activities.map(_.id).toSet
          isLastPage = activities.size < StravaActivityFeed.maxItemsPerPage || activityIds.intersect(lastActivityIds).nonEmpty
          next <- if (isLastPage) Future(activities.filter(a => !lastActivityIds.contains(a.id)) ++ accu) else list(page + 1, lastActivityIds, activities ++ accu)
        } yield next

        for {
          lastActivity <- storage.listRecentActivities(athleteId, StravaActivityFeed.maxItemsPerPage)
          lastActivityIds = lastActivity.map(_.id).toSet
          newActivities <- list(1, lastActivityIds, List.empty)
          _ = log.info(s"found ${newActivities.size} new activities")
        } yield newActivities

      case _ =>
        log.info(s"was already refreshed in the last $stalePeriodInMillis millis")
        Future(Iterable.empty)
    }
  }

}
