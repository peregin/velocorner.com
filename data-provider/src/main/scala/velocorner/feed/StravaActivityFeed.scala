package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.StandaloneWSResponse
import velocorner.SecretConfig
import velocorner.model.strava.{Activity, Athlete}
import velocorner.util.JsonIo

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Implementation to connect with Strava REST API
 */
object StravaActivityFeed extends LazyLogging {

  val baseUrl = "https://www.strava.com/api/v3"
  val accessTokenUrl = s"${StravaActivityFeed.baseUrl}/oauth/token"
  val authorizationUrl = s"${StravaActivityFeed.baseUrl}/oauth/authorize"

  val maxItemsPerPage = 200 // limitation from Strava

  def listRecentAthleteActivities(implicit feed: StravaActivityFeed): Future[List[Activity]] = feed.listAthleteActivities(1, StravaActivityFeed.maxItemsPerPage)

  def listAllAthleteActivities(implicit feed: ActivityFeed): Future[List[Activity]] = {

    def list(page: Int, accu: List[Activity]): Future[List[Activity]] = {
      for {
        activities <- feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
        _ = logger.debug(s"page $page, activities ${activities.size}")
        next <- if (activities.size < StravaActivityFeed.maxItemsPerPage) Future(activities ++ accu) else list(page + 1, activities ++ accu)
      } yield next
    }
    list(1, List.empty)
  }
}

class StravaActivityFeed(maybeToken: Option[String], val config: SecretConfig) extends HttpFeed with ActivityFeed with LazyLogging {

  val token = maybeToken.getOrElse(config.getToken("strava")) // dedicated token after authentication or application generic
  val clientId = config.getId("strava")
  logger.info(s"connecting to strava with token [$token] and clientId[$clientId]...")
  val authHeader = s"Bearer $token"

  // clubs
  override def listRecentClubActivities(clubId: Long): Future[List[Activity]] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/clubs/$clubId/activities").withHttpHeaders(("Authorization", authHeader)).get())
  } yield extractActivities(response)

  override def listClubAthletes(clubId: Long): Future[List[Athlete]] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/clubs/$clubId/members").withHttpHeaders(("Authorization", authHeader)).get())
  } yield JsonIo.read[List[Athlete]](response.body)

  // activities
  override def listAthleteActivities(page: Int, pageSize: Int = StravaActivityFeed.maxItemsPerPage): Future[List[Activity]] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/athlete/activities"))
      .withHttpHeaders(("Authorization", authHeader))
      .withQueryStringParameters(("page", page.toString), ("per_page", pageSize.toString))
      .get()
  } yield extractActivities(response)

  private def extractActivities(response: StandaloneWSResponse): List[Activity] = JsonIo.read[List[Activity]](response.body)

  // athlete
  override def getAthlete: Future[Athlete] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/athlete").withHttpHeaders(("Authorization", authHeader)).get())
  } yield JsonIo.read[Athlete](response.body)
}
