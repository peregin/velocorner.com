package velocorner.gateway

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}

/**
  * API Gateway
  * - single entry point to the stack
  * - expose API from schema, see app.apibuilder.io
  * - convert and proxy to internal requests (e.g. Thrift or TypeScript)
  * - authentication (e.g. JWT)
  * - mapping to the internal service topology, use service discovery for calling services
  * In addition:
  * - caching
  * - retry policies, circuit breaking
  * - throttling
  * - IP white/black-listing
  */
object GatewayService extends HttpServer {

  class InfoController extends Controller {
    get("/info") { request: Request =>
      response.ok.html("<h1>Hello</h1>")
    }
  }

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add[InfoController]
  }

}
