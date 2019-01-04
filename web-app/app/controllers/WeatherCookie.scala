package controllers

import org.apache.commons.codec.binary.Base64
import play.api.mvc.{AnyContent, Cookie, Request}

object WeatherCookie {

  val name = "weather_location"

  def create(location: String): Cookie = {
    val encoded = Base64.encodeBase64String(location.getBytes)
    Cookie(name, encoded)
  }

  def retrieve(implicit request: Request[AnyContent]): String = {
    request.cookies.get(name).map(c => Base64.decodeBase64(c.value)).map(new String(_)).getOrElse("")
  }
}
