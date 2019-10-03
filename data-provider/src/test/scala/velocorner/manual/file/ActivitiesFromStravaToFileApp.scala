package velocorner.manual.file

import java.io.PrintWriter

import com.typesafe.scalalogging.LazyLogging
import velocorner.SecretConfig
import velocorner.feed.{HttpFeed, StravaActivityFeed}
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.util.JsonIo


object ActivitiesFromStravaToFileApp extends App with AwaitSupport with LazyLogging with MyMacConfig {

  implicit val feed = new StravaActivityFeed(None, SecretConfig.load())
  //val activities = await(feed.recentClubActivities(Club.Velocorner))
  val activities = await(StravaActivityFeed.listRecentAthleteActivities)
  logger.info(s"got ${activities.size} athlete activities")

  val json = JsonIo.write(activities)
  val pw = new PrintWriter("/Users/levi/Downloads/strava/all.json")
  pw.print(json)
  pw.close()
  logger.info("file has been created")

  feed.close()
  HttpFeed.shutdown()
}
