package velocorner.manual

import java.io.PrintWriter

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.proxy.StravaFeed
import velocorner.util.JsonIo


object ActivitiesFromStravaToFileApp extends App with Logging with MyMacConfig {

  private val config = SecretConfig.load()
  val token = config.getApplicationToken
  val clientId = config.getApplicationId
  log.info(s"connecting to strava with token [$token] and clientId[$clientId]...")

  val feed = new StravaFeed(token, clientId)
  //val activities = feed.recentClubActivities(Club.Velocorner)
  val activities = feed.listAthleteActivities
  log.info(s"got ${activities.size} athlete activities")

  val json = JsonIo.write(activities)
  val pw = new PrintWriter("/Users/levi/Downloads/strava/all.json")
  pw.print(json)
  pw.close()
  log.info("file has been created")
}
