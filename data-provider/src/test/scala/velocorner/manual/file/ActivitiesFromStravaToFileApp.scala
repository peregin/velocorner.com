package velocorner.manual.file

import java.io.PrintWriter

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.util.JsonIo


object ActivitiesFromStravaToFileApp extends App with AwaitSupport with Logging with MyMacConfig {

  implicit val feed = new StravaActivityFeed(None, SecretConfig.load())
  //val activities = await(feed.recentClubActivities(Club.Velocorner))
  val activities = await(StravaActivityFeed.listRecentAthleteActivities)
  log.info(s"got ${activities.size} athlete activities")

  val json = JsonIo.write(activities)
  val pw = new PrintWriter("/Users/levi/Downloads/strava/all.json")
  pw.print(json)
  pw.close()
  log.info("file has been created")

  feed.close()
  HttpFeed.shutdown()
}
