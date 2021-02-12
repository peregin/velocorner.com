package velocorner.analytics

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.inject.Logging

import java.util.Date

object AnalyticsService extends HttpServer {

  class InfoController extends Controller with Logging {

    get("/") { request: Request =>
      response.ok.html(s"<h1>Welcome ${new Date()}</h1>")
    }

    get("/info") { request: Request =>
      response.ok.html("<h1>Hello from info</h1>")
    }
  }

  override def configureHttp(router: HttpRouter): Unit = {
    info(s"adding controller routes to $router")
    router.add[InfoController]
  }

}
