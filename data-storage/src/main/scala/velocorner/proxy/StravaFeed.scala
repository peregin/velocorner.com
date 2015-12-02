package velocorner.proxy

import com.ning.http.client.{ProxyServer, AsyncHttpClientConfig}
import org.slf4s.Logging
import play.api.http.{MimeTypes, HeaderNames}
import play.api.libs.ws.{WS, WSResponse}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import play.api.mvc.Results
import velocorner.SecretConfig
import velocorner.model.{Athlete, Authentication, Activity}
import velocorner.util.JsonIo

import scala.annotation.tailrec
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by levi on 17/03/15.
 */
class StravaFeed(maybeToken: Option[String], config: SecretConfig) extends Feed with Logging {

  val token = maybeToken.getOrElse(config.getApplicationToken) // dedicated token after authentication or application generic
  val clientId = config.getApplicationId
  log.info(s"connecting to strava with token [$token] and clientId[$clientId]...")

  val authHeader = s"Bearer $token"
  val timeout = 10 seconds
  val baseUrl = "https://www.strava.com"
  val maxItemsPerPage = 200 // limitation from strava

  val ningConfig = new NingAsyncHttpClientConfigBuilder().build()
  val httpConfigBuilder = new AsyncHttpClientConfig.Builder(ningConfig)

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
  
  override def getOAuth2Url(redirectHost: String): String = {
    s"$baseUrl/oauth/authorize?client_id=$clientId&response_type=code&redirect_uri=http://$redirectHost/oauth/callback&state=mystate&approval_prompt=force"
  }

  override def getOAuth2Token(code: String, clientSecret: String): Authentication = {
    val response = WS.clientUrl(s"$baseUrl/oauth/token")
      .withQueryString(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code
      )
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Results.EmptyContent())
    val result = Await.result(response, timeout)
    log.info(s"authentication reply ${result.statusText}")
    val access_token = (result.json \ "access_token").as[String]
    val athlete = (result.json \ "athlete").as[Athlete]
    Authentication(access_token, athlete)
  }

  override def recentClubActivities(clubId: Long): List[Activity] = {
    val response = WS.clientUrl(s"$baseUrl/api/v3/clubs/$clubId/activities").withHeaders(("Authorization", authHeader)).get()
    extractActivities(response)
  }

  override def recentAthleteActivities: List[Activity] = athleteActivities(1)

  override def listAthleteActivities: List[Activity] = {
    @tailrec
    def list(page: Int, accu: List[Activity]): List[Activity] = {
      val activities = athleteActivities(page)
      if (activities.size < maxItemsPerPage) activities ++ accu
      else list(page + 1, activities ++ accu)
    }
    list(1, List.empty)
  }

  private def athleteActivities(page: Int): List[Activity] = {
    val response = WS.clientUrl(s"$baseUrl/api/v3/athlete/activities")
      .withHeaders(("Authorization", authHeader))
      .withQueryString(("page", page.toString), ("per_page", maxItemsPerPage.toString))
      .get()
    extractActivities(response)
  }

  private def extractActivities(response: Future[WSResponse]): List[Activity] = {
    val json = Await.result(response, timeout).body
    JsonIo.read[List[Activity]](json)
  }
}
