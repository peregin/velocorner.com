package velocorner.manual

import com.typesafe.config.ConfigFactory
import velocorner.model.Club
import velocorner.proxy.StravaFeed


object ActivitiesFromStravaApp extends App {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val config = ConfigFactory.load()
  val token = config.getString("strava.application.token")
  println(s"connecting to strava with token [$token]...")

  val feed = new StravaFeed(token)
  val recent = feed.recentClubActivities(Club.Velocorner)
  println(s"got ${recent.size} club activities")
}
