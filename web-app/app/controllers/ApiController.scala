package controllers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}

import javax.inject.Inject
import org.reactivestreams.Subscriber
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Environment, Logger}
import velocorner.api.StatusInfo

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ApiController @Inject()(environment: Environment, val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with OriginChecker {

  val pings = new AtomicLong

  val allowedHosts: Seq[String] = connectivity.allowedHosts
  private val logger = Logger(getClass)

  // def mapped to /api/status
  def status = Action { implicit request =>
    val statusInfo = StatusInfo.compute(environment.mode, pings.get())
    Ok(Json.toJson(statusInfo))
  }

  // def mapped to /api/ping/
  def ping = Action { implicit request =>
    val counter = pings.incrementAndGet()
    val payload = request.contentType match {
      case Some("application/json") => request.body.asJson.toString
      case _ => request.body.asText.getOrElse("")
    }
    logger.info(s"PING[$counter]=[$payload]")
    Ok
  }

  // WebSocket to update the client
  // try with https://www.websocket.org/echo.html => ws://localhost:9000/ws
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
    //val out1 = Source.single("Welcome").concat(Source.maybe)
    val out = Source.fromPublisher((s: Subscriber[_ >: String]) => {
      logger.info(s"PUBLISH counter $counter")
      s.onNext(s"hello $counter")
      counter = counter + 1
    }).mapMaterializedValue{a =>
      logger.info(s"CONNECTED $a")
      a
    }.watchTermination() { (_, terminated) =>
      terminated.onComplete(_ => logger.info("DISCONNECTED"))
    }

    Flow.fromSinkAndSource(in, out)
  }
}
