package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.StandaloneWSResponse
import org.apache.pekko.stream.Materializer
import velocorner.{SecretConfig, ServiceProvider}
import velocorner.api.strava.Activity
import velocorner.model.strava.Athlete
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

  def listRecentAthleteActivities(implicit feed: StravaActivityFeed): Future[List[Activity]] =
    feed.listAthleteActivities(1, StravaActivityFeed.maxItemsPerPage)

  def listAllAthleteActivities(implicit feed: ActivityFeed): Future[List[Activity]] = {
    def list(page: Int, accu: List[Activity]): Future[List[Activity]] =
      for {
        activities <- feed.listAthleteActivities(page, StravaActivityFeed.maxItemsPerPage)
        _ = logger.debug(s"page $page, activities ${activities.size}")
        next <- if (activities.size < StravaActivityFeed.maxItemsPerPage) Future(activities ++ accu) else list(page + 1, activities ++ accu)
      } yield next
    list(1, List.empty)
  }
}

class StravaActivityFeed(
    maybeToken: Option[String],
    override val config: SecretConfig,
    override val materializer: Materializer = HttpFeed.materializer
) extends HttpFeed with ActivityFeed with LazyLogging {

  private val token =
    maybeToken.getOrElse(config.getAuthToken(ServiceProvider.Strava)) // dedicated token after authentication or application generic
  private val clientId = config.getAuthId(ServiceProvider.Strava)
  private val authHeader = s"Bearer $token"

  logger.info(s"connecting to strava with clientId[$clientId]...")

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

  override def exportRouteGpx(id: Long): Future[String] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/activities/$id/streams").withHttpHeaders(("Authorization", authHeader)))
      .withQueryStringParameters(
        ("keys", "latlng,time,altitude"),
        ("key_by_type", "true")
      )
      .get()
  } yield {
    ensureSuccessful(response)
    response.body
  }

  private def extractActivities(response: StandaloneWSResponse): List[Activity] = JsonIo.read[List[Activity]](response.body)

  private def ensureSuccessful(response: StandaloneWSResponse): Unit =
    if (response.status < 200 || response.status >= 300) {
      throw new IllegalStateException(s"Strava request failed with ${response.status}: ${response.statusText}")
    }

  // athlete
  override def getAthlete: Future[Athlete] = for {
    response <- ws(_.url(s"${StravaActivityFeed.baseUrl}/athlete").withHttpHeaders(("Authorization", authHeader)).get())
  } yield JsonIo.read[Athlete](response.body)
}
