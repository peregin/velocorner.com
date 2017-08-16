package velocorner.feed

import org.slf4s.Logging
import play.api.libs.ws.StandaloneWSResponse
import velocorner.SecretConfig
import velocorner.model.{Activity, Athlete, Statistics}
import velocorner.util.JsonIo

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Implementation to connect with Strava REST API
 */
object StravaActivityFeed {

  val baseUrl = "https://www.strava.com"
  val accessTokenUrl = s"${StravaActivityFeed.baseUrl}/oauth/token"
  val authorizationUrl = s"${StravaActivityFeed.baseUrl}/oauth/authorize"

  val maxItemsPerPage = 200 // limitation from Strava

  def listRecentAthleteActivities(implicit feed: StravaActivityFeed): List[Activity] = feed.listAthleteActivities(1, StravaActivityFeed.maxItemsPerPage)

  def listAllAthleteActivities(implicit feed: StravaActivityFeed): List[Activity] = {
    @tailrec
    def list(page: Int, accu: List[Activity]): List[Activity] = {
      val activities = feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
      if (activities.size < StravaActivityFeed.maxItemsPerPage) activities ++ accu
      else list(page + 1, activities ++ accu)
    }
    list(1, List.empty)
  }
}

class StravaActivityFeed(maybeToken: Option[String], val config: SecretConfig) extends HttpFeed with ActivityFeed with Logging {

  val token = maybeToken.getOrElse(config.getToken("strava")) // dedicated token after authentication or application generic
  val clientId = config.getId("strava")
  log.info(s"connecting to strava with token [$token] and clientId[$clientId]...")
  val authHeader = s"Bearer $token"
  val timeout = 10 seconds

  // clubs
  override def listRecentClubActivities(clubId: Long): List[Activity] = {
    val response = ws(_.url(s"${StravaActivityFeed.baseUrl}/api/v3/clubs/$clubId/activities").withHttpHeaders(("Authorization", authHeader)).get())
    extractActivities(response)
  }

  override def listClubAthletes(clubId: Long): List[Athlete] = {
    val response = ws(_.url(s"${StravaActivityFeed.baseUrl}/api/v3/clubs/$clubId/members").withHttpHeaders(("Authorization", authHeader)).get())
    val json = Await.result(response, timeout).body
    JsonIo.read[List[Athlete]](json)
  }

  // activities
  override def listAthleteActivities(page: Int, pageSize: Int = StravaActivityFeed.maxItemsPerPage): List[Activity] = {
    val response = ws(_.url(s"${StravaActivityFeed.baseUrl}/api/v3/athlete/activities"))
      .withHttpHeaders(("Authorization", authHeader))
      .withQueryStringParameters(("page", page.toString), ("per_page", pageSize.toString))
      .get()
    extractActivities(response)
  }

  private def extractActivities(response: Future[StandaloneWSResponse]): List[Activity] = {
    val json = Await.result(response, timeout).body
    JsonIo.read[List[Activity]](json)
  }

  // athlete
  override def getAthlete: Athlete = {
    val response = ws(_.url(s"${StravaActivityFeed.baseUrl}/api/v3/athlete").withHttpHeaders(("Authorization", authHeader)).get())
    val json = Await.result(response, timeout).body
    JsonIo.read[Athlete](json)
  }

  // year to date and overall stats - must be the logged in athlete id
  override def getStatistics(athleteId: Int): Statistics = {
    val response = ws(_.url(s"${StravaActivityFeed.baseUrl}/api/v3/athletes/$athleteId/stats").withHttpHeaders(("Authorization", authHeader)).get())
    val json = Await.result(response, timeout).body
    JsonIo.read[Statistics](json)
  }
}
