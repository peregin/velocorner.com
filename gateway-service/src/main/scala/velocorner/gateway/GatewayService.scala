package velocorner.gateway

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}

object GatewayService extends HttpServer {

  class InfoController extends Controller {
    get("/info") { request: Request =>
      response.ok
    }
  }

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add[InfoController]
  }

}
