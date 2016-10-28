package velocorner.manual.club

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.manual.MyMacConfig
import velocorner.model.Club
import velocorner.feed.StravaActivityFeed

object ClubActivitiesFromStravaToConsoleApp extends App with Logging with MyMacConfig {

  val feed = new StravaActivityFeed(None, SecretConfig.load())
  val activities = feed.listRecentClubActivities(Club.Velocorner)

  activities.foreach{a =>
    log.info(s"[${a.start_date_local}] ${a.athlete} -> ${a.distance / 1000} km")
  }

  log.info(s"got ${activities.size} club activities")

}
