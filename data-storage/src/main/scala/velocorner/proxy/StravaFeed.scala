package velocorner.proxy

import com.ning.http.client.{AsyncHttpClientConfigBean, AsyncHttpClientConfig, ProxyServer}
import org.slf4s.Logging
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}
import play.api.libs.ws.{WS, WSResponse}
import velocorner.SecretConfig
import velocorner.model.{Activity, Athlete}
import velocorner.util.JsonIo

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Created by levi on 17/03/15.
 */
object StravaFeed {
  val baseUrl = "https://www.strava.com"
  val accessTokenUrl = s"${StravaFeed.baseUrl}/oauth/token"
  val authorizationUrl = s"${StravaFeed.baseUrl}/oauth/authorize"

  val maxItemsPerPage = 200 // limitation from Strava

  def listRecentAthleteActivities(implicit feed: StravaFeed): List[Activity] = feed.listAthleteActivities(1, StravaFeed.maxItemsPerPage)

  def listAllAthleteActivities(implicit feed: StravaFeed): List[Activity] = {
    @tailrec
    def list(page: Int, accu: List[Activity]): List[Activity] = {
      val activities = feed.listAthleteActivities(page, StravaFeed.maxItemsPerPage)
      if (activities.size < StravaFeed.maxItemsPerPage) activities ++ accu
      else list(page + 1, activities ++ accu)
    }
    list(1, List.empty)
  }
}

class StravaFeed(maybeToken: Option[String], config: SecretConfig) extends Feed with Logging {

  val token = maybeToken.getOrElse(config.getApplicationToken) // dedicated token after authentication or application generic
  val clientId = config.getApplicationId
  log.info(s"connecting to strava with token [$token] and clientId[$clientId]...")

  val authHeader = s"Bearer $token"
  val timeout = 10 seconds

  val httpConfigSetup = if (config.getProxyHost.isDefined) new AsyncHttpClientConfigBean() else new NingAsyncHttpClientConfigBuilder().build()
  val httpConfigBuilder = new AsyncHttpClientConfig.Builder(httpConfigSetup)

  // setup secure proxy if it is configured w/o authentication
  for (proxyHost <- config.getProxyHost; proxyPort <- config.getProxyPort) {
    val proxyServer = (config.getProxyUser, config.getProxyPassword) match {
      case (Some(proxyUser), Some(proxyPassword)) => new ProxyServer(ProxyServer.Protocol.HTTPS, proxyHost, proxyPort, proxyUser, proxyPassword)
      case _ => new ProxyServer(ProxyServer.Protocol.HTTPS, proxyHost, proxyPort)
    }
    httpConfigBuilder.setProxyServer(proxyServer)
  }

  implicit val wsClient = new NingWSClient(httpConfigBuilder.build())
  implicit val executionContext = ExecutionContext.Implicits.global


  // clubs
  override def listRecentClubActivities(clubId: Long): List[Activity] = {
    val response = WS.clientUrl(s"${StravaFeed.baseUrl}/api/v3/clubs/$clubId/activities").withHeaders(("Authorization", authHeader)).get()
    extractActivities(response)
  }

  override def listClubAthletes(clubId: Long): List[Athlete] = {
    val response = WS.clientUrl(s"${StravaFeed.baseUrl}/api/v3/clubs/$clubId/members").withHeaders(("Authorization", authHeader)).get()
    val json = Await.result(response, timeout).body
    JsonIo.read[List[Athlete]](json)
  }

  // activities
  override def listAthleteActivities(page: Int, pageSize: Int = StravaFeed.maxItemsPerPage): List[Activity] = {
    val response = WS.clientUrl(s"${StravaFeed.baseUrl}/api/v3/athlete/activities")
      .withHeaders(("Authorization", authHeader))
      .withQueryString(("page", page.toString), ("per_page", pageSize.toString))
      .get()
    extractActivities(response)
  }

  private def extractActivities(response: Future[WSResponse]): List[Activity] = {
    val json = Await.result(response, timeout).body
    JsonIo.read[List[Activity]](json)
  }

  // athlete
  override def getAthlete: Athlete = {
    val response = WS.clientUrl(s"${StravaFeed.baseUrl}/api/v3/athlete").withHeaders(("Authorization", authHeader)).get()
    Await.result(response, timeout).json.as[Athlete]
  }
}
