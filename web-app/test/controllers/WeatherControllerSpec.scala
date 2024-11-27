package controllers

import org.apache.pekko.util.Timeout
import cats.implicits.catsSyntaxOptionId
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.api.weather.CurrentWeather
import velocorner.api.GeoPosition
import velocorner.feed.WeatherFeed
import velocorner.model.DateTimePattern
import velocorner.model.weather.WeatherResponse
import velocorner.storage.{LocationStorage, Storage}
import velocorner.util.JsonIo

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

//noinspection TypeAnnotation
class WeatherControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for weather forecast" should {

    implicit val timeout = new Timeout(10 seconds)

    val nowTxt = "2021-04-17T17:57:56Z"
    val now = DateTime.parse(nowTxt, DateTimeFormat.forPattern(DateTimePattern.longFormat))
    lazy val weatherFixture = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/current.json")

    val settingsMock = mock[ConnectivitySettings]
    val storageMock = mock[Storage[Future]]
    val locationStorage = mock[LocationStorage[Future]]

    when(settingsMock.getStorage).thenReturn(storageMock)
    when(storageMock.getLocationStorage).thenReturn(locationStorage)
    when(
      locationStorage.store(
        "Zurich",
        GeoPosition(
          latitude = weatherFixture.coord.get.lat,
          longitude = weatherFixture.coord.get.lon
        )
      )
    )
      .thenReturn(Future.unit)
    val currentWeatherFixture = CurrentWeather(
      location = "Zurich",
      timestamp = now,
      bootstrapIcon = "icon",
      current = weatherFixture.weather.get.head,
      info = weatherFixture.main.get,
      sunriseSunset = weatherFixture.sys.get,
      coord = weatherFixture.coord.get
    )

    val controller = new WeatherController(settingsMock, stubControllerComponents()) {
      override def clock(): DateTime = now
      override lazy val weatherFeed: WeatherFeed[Future] = (location: String) => Future.successful(currentWeatherFixture.some)
    }

    "retrieve current weather" in {
      val result = controller.current("Zurich").apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val currentWeather = Helpers.contentAsJson(result).as[CurrentWeather]
      currentWeather mustBe currentWeatherFixture
    }

    "fail for empty place in current weather" in {
      val result = controller.current("").apply(FakeRequest())
      Helpers.status(result) mustBe Status.BAD_REQUEST
    }

    "test cookie encoding/decoding" in {
      val cookie = WeatherCookie.create("a location")
      cookie.value mustBe "YSBsb2NhdGlvbg=="
      val location = WeatherCookie.retrieve(FakeRequest().withCookies(cookie))
      location mustBe "a location"
    }
  }
}
