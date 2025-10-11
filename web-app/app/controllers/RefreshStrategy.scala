package controllers

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.Logger
import velocorner.api.Account
import velocorner.api.strava.Activity
import velocorner.feed.{ActivityFeed, StravaActivityFeed}
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// for kestrel combinator
import mouse.all._

/**
 * Isolate the update the logic to refresh club and account activities.
 */
@Singleton
class RefreshStrategy @Inject() (connectivity: ConnectivitySettings) {

  private val log = Logger.of(this.getClass)

  // won't refresh from the feed if was updated within this time period
  val stalePeriodInMillis = 60000 // more than a minute

  // query from the storage and eventually from the activity feed
  def refreshAccountActivities(account: Account, now: DateTime): Future[Iterable[Activity]] =
    account.stravaAccess
      .map(_.accessToken)
      .map { accessToken =>
        // allow refresh after some time only
        val storage = connectivity.getStorage
        val feed = connectivity.getStravaFeed(accessToken)
        log.info(s"refresh for athlete: ${account.athleteId}, last update: ${account.lastUpdate}")

        val activitiesF = for {
          newActivities: Iterable[Activity] <- retrieveNewActivities(feed, storage, account.athleteId, account.lastUpdate, now)
          // log the most recent activity
          maybeMostRecent = newActivities.map(_.start_date).toSeq.sortWith((a, b) => a.compareTo(b) > 0).headOption
          _ = log.info(s"most recent activity retrieved is from $maybeMostRecent")
          _ <- storage.storeActivity(newActivities)
          _ <- storage.getAccountStorage.store(account.copy(lastUpdate = Some(now)))
        } yield newActivities
        activitiesF <| (_.onComplete(_ => feed.close()))
      }
      .getOrElse(Future(Iterable.empty))

  protected def retrieveNewActivities(
      feed: ActivityFeed,
      storage: Storage[Future],
      athleteId: Long,
      lastUpdate: Option[DateTime],
      now: DateTime
  ): Future[Iterable[Activity]] =
    lastUpdate.map(_.getMillis) match {

      case None => // it was never synchronized, do a full update
        log.info(s"retrieving all activities as it was never synched")
        StravaActivityFeed.listAllAthleteActivities(feed)

      case Some(lastUpdateInMillis) if now.getMillis - lastUpdateInMillis > stalePeriodInMillis =>
        log.info(s"retrieving latest activities for $athleteId")

        def list(page: Int, lastActivityIds: Set[Long], accu: Iterable[Activity]): Future[Iterable[Activity]] = for {
          activities <- feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
          _ = log.debug(s"refresh page $page, activities ${activities.size}")
          activityIds = activities.map(_.id).toSet
          isLastPage = activities.size < StravaActivityFeed.maxItemsPerPage || activityIds.intersect(lastActivityIds).nonEmpty
          next <-
            if (isLastPage) Future(activities.filter(a => !lastActivityIds.contains(a.id)) ++ accu)
            else list(page + 1, lastActivityIds, activities ++ accu)
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
