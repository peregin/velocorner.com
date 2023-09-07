package velocorner.manual.club

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.manual.{AwaitSupport, MyLocalConfig}
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.model.strava.Club
import velocorner.util.CloseableResource

object ClubActivitiesFromStravaToConsoleApp extends App with LazyLogging with CloseableResource with AwaitSupport with MyLocalConfig {

  withCloseable(new StravaActivityFeed(None, SecretConfig.load())) { feed =>
    val activities = awaitOn(feed.listRecentClubActivities(Club.velocornerId))
    activities.foreach { a =>
      logger.info(s"[${a.start_date_local}] ${a.athlete} -> ${a.distance / 1000} km")
    }
    logger.info(s"got ${activities.size} club activities")
  }
  HttpFeed.shutdown()
}
