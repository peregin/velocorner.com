package controllers

import play.api.mvc.{AnyContent, Cookie, Request}

import concurrent.duration._
import scala.language.postfixOps
import cats.implicits._

import java.util.Base64

object WeatherCookie {

  // keep it for a week
  private val cookieAgeDuration = 7 days
  val name = "weather_location"

  def create(location: String): Cookie = {
    val encoded = Base64.getEncoder.encodeToString(location.getBytes)
    Cookie(name, encoded, maxAge = cookieAgeDuration.toSeconds.toInt.some)
  }

  def retrieve(implicit request: Request[AnyContent]): String =
    request.cookies.get(name).map(c => Base64.getDecoder.decode(c.value)).map(new String(_)).getOrElse("")
}
