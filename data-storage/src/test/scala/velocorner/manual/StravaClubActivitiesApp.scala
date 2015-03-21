package velocorner.manual

import velocorner.model.Club
import velocorner.proxy.StravaFeed


object StravaClubActivitiesApp extends App {

  if (args.length < 1) sys.error("strava token must be provided")
  val token = args(0)
  println(s"connecting to strava with token [$token]...")

  val feed = new StravaFeed(token)
  val recent = feed.recentClubActivities(Club.Velocorner)
  println(s"got ${recent.size} club activities")
}
