package controllers

import org.apache.commons.codec.binary.Base64
import play.api.mvc.{AnyContent, Cookie, Request}

import concurrent.duration._
import scala.language.postfixOps

import cats.implicits._

object WeatherCookie {

  // keep it for a week
  val cookieAgeDuration = 7 days
  val name = "weather_location"

  def create(location: String): Cookie = {
    val encoded = Base64.encodeBase64String(location.getBytes)
    Cookie(name, encoded, maxAge = cookieAgeDuration.toSeconds.toInt.some)
  }

  def retrieve(implicit request: Request[AnyContent]): String = {
    request.cookies.get(name).map(c => Base64.decodeBase64(c.value)).map(new String(_)).getOrElse("")
  }
}
