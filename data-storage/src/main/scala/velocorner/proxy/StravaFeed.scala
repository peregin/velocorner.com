package velocorner.proxy

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.{WS, WSResponse}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import velocorner.model.{Authentication, Activity}
import velocorner.util.JsonIo

import scala.annotation.tailrec
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by levi on 17/03/15.
 */
class StravaFeed(token: String, clientId: String) extends Feed {

  val authHeader = "Bearer " + token
  val timeout = 10 seconds
  val baseUrl = "https://www.strava.com"
  val maxItemsPerPage = 200 // limitation from strava

  val config = new NingAsyncHttpClientConfigBuilder().build()
  val builder = new AsyncHttpClientConfig.Builder(config)
  implicit val wsClient = new NingWSClient(builder.build())
  implicit val executionContext = ExecutionContext.Implicits.global

  override def getOAuth2Url(redirectUri: String): String = {
    s"$baseUrl/oauth/authorize?client_id=@$clientId&response_type=code&redirect_uri=http://$redirectUri/oauth/callback&state=mystate&approval_prompt=force"
  }

  override def getOAuth2Token(code: String, clientSecret: String): Authentication = {
    val response = WS.clientUrl(s"$baseUrl/oauth/token")
      .withQueryString(
        ("client_id", clientId),
        ("client_secret", clientSecret),
        ("code", code)
      ).get()
    val json = Await.result(response, timeout).body
    JsonIo.read[Authentication](json)
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
