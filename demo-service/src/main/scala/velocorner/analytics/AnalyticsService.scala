package velocorner.analytics

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}

object AnalyticsService extends HttpServer {

  class InfoController extends Controller {
    get("/info") { request: Request =>
      response.ok.html("<h1>Hello</h1>")
    }
  }

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add[InfoController]
  }

}
