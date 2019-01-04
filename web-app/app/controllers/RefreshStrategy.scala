package controllers

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import org.slf4s
import org.slf4s.Logging
import play.Logger
import velocorner.model.Account
import velocorner.feed.StravaActivityFeed
import velocorner.model.strava.Activity
import velocorner.util.CloseableResource

import scala.annotation.tailrec

/**
  * Isolate the update the logic to refresh club and account activities.
  */
@Singleton
class RefreshStrategy @Inject()(connectivity: ConnectivitySettings) extends Logging with CloseableResource {

  override val log = new slf4s.Logger(Logger.underlying())

  def refreshAccountActivities(account: Account) {
    // allow refresh after some time only
    val now = DateTime.now()
    val storage = connectivity.getStorage
    val newActivities = withCloseable(connectivity.getFeed(account.accessToken)) { feed =>

      log.info(s"refresh for athlete: ${account.athleteId}, last update: ${account.lastUpdate}")

      account.lastUpdate.map(_.getMillis) match {

        case None => // it was never synched, do a full update
          log.info(s"retrieving all activities for ${account.athleteId}")
          val activities = StravaActivityFeed.listAllAthleteActivities(feed)
          log.info(s"found ${activities.size} overall activities")
          activities

        case Some(lastUpdateInMillis) if now.getMillis - lastUpdateInMillis > 60000 => // more than a minute
          log.info(s"retrieving latest activities for ${account.athleteId}")
          val lastActivityIds = storage.listRecentActivities(account.athleteId, StravaActivityFeed.maxItemsPerPage).map(_.id).toSet

          @tailrec
          def list(page: Int, accu: Iterable[Activity]): Iterable[Activity] = {
            val activities = feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
            val activityIds = activities.map(_.id).toSet
            if (activities.size < StravaActivityFeed.maxItemsPerPage || activityIds.intersect(lastActivityIds).nonEmpty) activities.filter(a => !lastActivityIds.contains(a.id)) ++ accu
            else list(page + 1, activities ++ accu)
          }

          val newActivities = list(1, List.empty)
          log.info(s"found ${newActivities.size} new activities")
          newActivities

        case _ =>
          log.info("was already refreshed in the last minute")
          Iterable.empty
      }
    }

    // log the most recent activity
    val maybeMostRecent = newActivities.map(_.start_date).toSeq.sortWith((a, b) => a.compareTo(b) > 0).headOption
    log.info(s"most recent activity retrieved is from $maybeMostRecent")

    storage.store(newActivities)
    storage.store(account.copy(lastUpdate = Some(now)))
  }

}
