package controllers

import org.joda.time.DateTime
import org.slf4s
import org.slf4s.Logging
import play.Logger
import velocorner.model.{Account, Activity, Club}
import velocorner.proxy.StravaFeed

import scala.annotation.tailrec

/**
  * Isolate the update the logic to refresh club and account activities.
  */
object RefreshStrategy extends Logging {

  override val log = new slf4s.Logger(Logger.underlying())

  @volatile var lastClubUpdateTs = 0L

  def refreshClubActivities() {
    val nowInMillis = DateTime.now().getMillis
    val diffInMillis = nowInMillis - lastClubUpdateTs
    lastClubUpdateTs = nowInMillis
    if (diffInMillis > 1200000) {
      Logger.info("refreshing club information from Stava")
      // update from Strava
      val feed = Global.getFeed
      val storage = Global.getStorage
      val clubActivities = feed.listRecentClubActivities(Club.Velocorner)
      storage.store(clubActivities)
      val clubAthletes = feed.listClubAthletes(Club.Velocorner)
      clubAthletes.foreach(storage.store)
      val club = Club(Club.Velocorner, clubAthletes.map(_.id))
      storage.store(club)
    }
  }

  def refreshAccountActivities(account: Account) {
    // allow refresh after some time only
    val storage = Global.getStorage
    val feed = Global.getFeed(account.accessToken)
    val now = DateTime.now()
    val newActivities = account.lastUpdate.map(_.getMillis) match {

      case None => // it was never synched, do a full update
        log.info(s"retrieving all activities for ${account.athleteId}")
        val activities = StravaFeed.listAllAthleteActivities(feed)
        log.info(s"found ${activities.size} activities")
        activities

      case Some(lastUpdateInMillis) if now.getMillis - lastUpdateInMillis > 60000 =>
        log.info(s"retrieving latest activities for ${account.athleteId}")
        val lastActivityIds = storage.listRecentActivities(account.athleteId, StravaFeed.maxItemsPerPage).map(_.id).toSet

        @tailrec
        def list(page: Int, accu: Iterable[Activity]): Iterable[Activity] = {
          val activities = feed.listAthleteActivities(page, StravaFeed.maxItemsPerPage)
          val activityIds = activities.map(_.id).toSet
          if (activities.size < StravaFeed.maxItemsPerPage || activityIds.intersect(lastActivityIds).nonEmpty) activities.filter(a => !lastActivityIds.contains(a.id)) ++ accu
          else list(page + 1, activities ++ accu)
        }
        val newActivities = list(1, List.empty)
        log.info(s"found ${newActivities.size} new activities")
        newActivities
    }

    storage.store(newActivities)
    storage.store(account.copy(lastUpdate = Some(now)))
  }

}
