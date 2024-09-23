package controllers

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}

import javax.inject.{Inject, Singleton}
import org.reactivestreams.Subscriber
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.mvc._
import play.api.{Environment, Logger}
import velocorner.api.StatusInfo
import velocorner.search.BrandSearch

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApiController @Inject() (environment: Environment, val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with OriginChecker {

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

  // WebSocket to update the client
  // try with https://www.websocket.org/echo.html => ws://localhost:9000/api/ws
  def ws: WebSocket = WebSocket.acceptOrResult[String, String] {
    case rh if sameOriginCheck(rh) =>
      logger.info(s"ws with request header: $rh")
      val flow = wsFlow(rh)
      Future.successful[Either[Result, Flow[String, String, _]]](Right(flow)).recover { case e =>
        logger.error("failed to create websocket", e)
        Left(InternalServerError(s"failed to create websocket, ${e.getMessage}"))
      }
    case rejected =>
      logger.error(s"same origin check failed for $rejected")
      Future.successful(Left(Forbidden))
  }

  var counter = 1
  private def wsFlow(rh: RequestHeader): Flow[String, String, NotUsed] = {
    // input, just echo the input
    val in = Sink.foreach[String](println)

    // output, use a publisher
    // val out1 = Source.single("Welcome").concat(Source.maybe)
    val out = Source
      .fromPublisher { (s: Subscriber[_ >: String]) =>
        logger.info(s"PUBLISH counter $counter")
        s.onNext(s"hello $counter")
        counter = counter + 1
      }
      .mapMaterializedValue { a =>
        logger.info(s"CONNECTED $a")
        a
      }
      .watchTermination() { (_, terminated) =>
        terminated.onComplete(_ => logger.info("DISCONNECTED"))
      }

    Flow.fromSinkAndSource(in, out)
  }
}
