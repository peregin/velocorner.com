package controllers

import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest

import scala.language.postfixOps

//noinspection TypeAnnotation
class WeatherCookieSpec extends PlaySpec {

  "test cookie encoding/decoding" in {
    val cookie = WeatherCookie.create("a location")
    cookie.value mustBe "YSBsb2NhdGlvbg=="
    val location = WeatherCookie.retrieve(FakeRequest().withCookies(cookie))
    location mustBe "a location"
  }

}
