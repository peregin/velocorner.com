package velocorner.analytics

import argonaut.Json
import argonaut.Json.{jArray, jObject, jString}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.inject.Logging

import java.util.Date

object InfoService extends HttpServer {

  sys.props += "io.netty.tryReflectionSetAccessible" -> "true"

  class InfoController extends Controller with Logging {

    get("/") { request: Request =>
      response.ok.html(s"<h1>Welcome ${new Date()}</h1>")
    }

    get("/hash") { request: Request =>
      val gitHash = test.service.scala.build.BuildInfo.gitHash
      val reply = Json.obj(
        ("hash" -> jString(gitHash))
      )
      response.ok.json(reply.spaces2)
    }
  }

  override def configureHttp(router: HttpRouter): Unit = {
    info(s"adding controller routes to $router")
    router.add[InfoController]
  }

}
