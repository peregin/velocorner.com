package controllers

import java.net.URI

import play.api.Logger
import play.api.mvc.RequestHeader

trait OriginChecker {

  private val logger = Logger(getClass)

  def allowedHosts: Seq[String]

  /**
   * Checks that the WebSocket comes from the same origin.  This is necessary to protect
   * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
   *
   * See https://tools.ietf.org/html/rfc6455#section-1.3 and
   * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
   */
  def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    val maybeOrigin = rh.headers.get("Origin")
    maybeOrigin match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
   * Returns true if the value of the Origin header contains an acceptable value.
   */
  private def originMatches(origin: String): Boolean =
    try {
      val url = new URI(origin)
      allowedHosts.exists(_.equalsIgnoreCase(url.getHost))
    } catch {
      case e: Exception => false
    }

}
