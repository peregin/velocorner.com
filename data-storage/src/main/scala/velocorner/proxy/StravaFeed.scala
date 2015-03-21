package velocorner.proxy

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.{WS, WSResponse, DefaultWSClientConfig}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._

/**
 * Created by levi on 17/03/15.
 */
class StravaFeed(token: String) extends Feed {

  val authHeader = "Bearer " + token
  val timeout = 10 seconds
  val baseUrl = "https://www.strava.com"

  val config = new NingAsyncHttpClientConfigBuilder(DefaultWSClientConfig()).build()
  val builder = new AsyncHttpClientConfig.Builder(config)
  implicit val wsClient = new NingWSClient(builder.build())
  implicit val executionContext = ExecutionContext.Implicits.global

  override def recentClubActivities(clubId: Long): List[Activity] = {
    val response: Future[WSResponse] = WS.clientUrl(s"$baseUrl/api/v3/clubs/$clubId/activities").withHeaders(("Authorization", authHeader)).get()
    val json = Await.result(response, timeout).body
    println(json)
    JsonIo.read[List[Activity]](json)
  }

  override def recentAthleteActivities: List[Activity] = ???

  override def listAthleteActivities: List[Activity] = ???

}
