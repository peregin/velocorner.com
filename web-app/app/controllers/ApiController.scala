package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.mvc._
import play.api.{Environment, Logger}
import velocorner.api.StatusInfo

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ApiController @Inject() (environment: Environment, val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components) {

  val pings = new AtomicLong

  val allowedHosts: Seq[String] = connectivity.allowedHosts
  private val logger = Logger(getClass)

  // def mapped to /api/status
  def status: Action[AnyContent] = Action { _ =>
    val statusInfo = StatusInfo.compute(environment.mode, pings.get())
    Ok(Json.toJson(statusInfo))
  }

  // def mapped to /api/ping/ - expects text or json
  def ping: Action[AnyContent] = Action { implicit request =>
    val counter = pings.incrementAndGet()
    val maybePayload = request.contentType match {
      case Some("application/json") => request.body.asJson.map(_.toString)
      case _                        => request.body.asText
    }
    // log only if payload was sent with a POST
    if (maybePayload.nonEmpty) {
      val remoteAddress = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress)
      logger.info(s"PING[$counter]=[$maybePayload], remote=$remoteAddress")
    }
    // being monitored as keyword "ok" in https://uptimerobot.com/
    Ok(
      JsObject(
        Seq(
          "status" -> JsString("ok"),
          "count" -> JsNumber(counter)
        )
      )
    )
  }
}
