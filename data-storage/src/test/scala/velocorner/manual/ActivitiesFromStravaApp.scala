package velocorner.manual

import java.io.PrintWriter

import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.util.JsonIo


object ActivitiesFromStravaApp extends App {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/strava/velocorner.conf"

  val token = SecretConfig.getApplicationToken
  println(s"connecting to strava with token [$token]...")

  val feed = new StravaFeed(token)
  //val activities = feed.recentClubActivities(Club.Velocorner)
  val activities = feed.listAthleteActivities
  println(s"got ${activities.size} athlete activities")

  val json = JsonIo.write(activities)
  val pw = new PrintWriter("/Users/levi/Downloads/strava/all.json")
  pw.print(json)
  pw.close()
  println("file has been created")
}
