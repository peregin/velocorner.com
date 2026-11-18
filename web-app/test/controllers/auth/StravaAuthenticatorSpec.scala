package controllers.auth

import ch.qos.logback.classic.{Logger, spi}
import ch.qos.logback.core.read.ListAppender
import controllers.ConnectivitySettings
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSResponse
import velocorner.{SecretConfig, ServiceProvider}

import java.io.{PrintWriter, StringWriter}
import scala.jdk.CollectionConverters._

class StravaAuthenticatorSpec extends AnyWordSpec with Matchers {

  "Strava token parsing" should {
    Seq("valid", "invalid field", "invalid JSON", "invalid authorization").foreach { scenario =>
      s"keep credentials out of logs and exceptions ($scenario)" in {
        val connectivity = mock(classOf[ConnectivitySettings])
        val config = mock(classOf[SecretConfig])
        when(connectivity.secretConfig).thenReturn(config)
        when(config.getAuthSecret(ServiceProvider.Strava)).thenReturn("client-secret-sentinel")
        when(config.getAuthId(ServiceProvider.Strava)).thenReturn("client-id")
        val authenticator = new StravaAuthenticator(connectivity)
        val response = mock(classOf[StandaloneWSResponse])
        val accessToken = "access-token-sentinel"
        val refreshToken = "refresh-token-sentinel"
        val tokenBody = Json.obj(
          "access_token" -> accessToken,
          "refresh_token" -> refreshToken,
          "expires_in" -> (if (scenario == "invalid field") Json.toJson(refreshToken) else Json.toJson(3600))
        )
        val body =
          if (scenario == "invalid authorization") Json.obj("errors" -> Json.arr(Json.obj("code" -> "invalid", "field" -> accessToken)))
          else tokenBody
        when(response.status).thenReturn(200)
        when(response.body).thenReturn(body.toString)
        when(response.toString).thenReturn(body.toString)
        if (scenario == "invalid JSON") {
          when(response.body[JsValue]).thenThrow(new IllegalArgumentException(s"Invalid JSON: $accessToken $refreshToken"))
        } else {
          when(response.body[JsValue]).thenReturn(body)
        }

        val logger = LoggerFactory.getLogger(classOf[StravaAuthenticator]).asInstanceOf[Logger]
        val appender = new ListAppender[spi.ILoggingEvent]()
        appender.start()
        logger.addAppender(appender)
        try {
          val stackTrace = new StringWriter()
          if (scenario == "valid") {
            val result = authenticator.parseAccessTokenResponse(response)
            result.accessToken mustBe accessToken
            result.refreshToken mustBe refreshToken
          } else {
            val error = intercept[IllegalArgumentException](authenticator.parseAccessTokenResponse(response))
            error.getMessage must include("status 200")
            if (scenario == "invalid authorization") error.getMessage must include("\"code\":\"invalid\"")
            error.printStackTrace(new PrintWriter(stackTrace))
          }
          val diagnostics = appender.list.asScala.map(_.getFormattedMessage).mkString("\n") + stackTrace.toString
          diagnostics must not include accessToken
          diagnostics must not include refreshToken
          diagnostics must not include "client-secret-sentinel"
        } finally {
          logger.detachAppender(appender)
          appender.stop()
        }
      }
    }
  }
}
