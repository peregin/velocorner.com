package controllers.util

import com.typesafe.scalalogging.LazyLogging
import play.api.{Environment, Mode}
import play.api.mvc.{AnyContent, Request}

trait RemoteIp extends LazyLogging {

  def detectIp(request: Request[AnyContent], environment: Environment): String = {
    val remoteAddress = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress) match {
      case "0:0:0:0:0:0:0:1" if environment.mode == Mode.Dev => "85.1.45.35"
      case "0:0:0:0:0:0:0:1"                                 => "127.0.0.1"
      case other                                             => other
    }
    logger.debug(s"remote address is $remoteAddress")
    remoteAddress
  }
}
