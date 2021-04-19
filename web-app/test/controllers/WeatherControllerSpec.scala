package controllers

import akka.util.Timeout
import cats.implicits.catsSyntaxOptionId
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.api.weather.{CurrentWeather, DailyWeather, WeatherForecast}
import velocorner.model.DateTimePattern
import velocorner.model.weather.{ForecastResponse, WeatherResponse}
import velocorner.storage.{AttributeStorage, Storage}
import velocorner.util.JsonIo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class WeatherControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for club activity series" should {

    implicit val timeout = new Timeout(10 seconds)

    val nowTxt = "2021-04-17T17:57:56Z"
    val now = DateTime.parse(nowTxt, DateTimeFormat.forPattern(DateTimePattern.longFormat))
    lazy val forecastFixture = JsonIo.readReadFromResource[ForecastResponse]("/data/weather/forecast.json")
    lazy val weatherFixture = JsonIo.readReadFromResource[WeatherResponse]("/data/weather/current.json")

    val settingsMock = mock[ConnectivitySettings]
    val storageMock = mock[Storage[Future]]
    val attributeStorage = mock[AttributeStorage[Future]]
    val weatherStorage = mock[storageMock.WeatherStorage]

    when(settingsMock.getStorage).thenReturn(storageMock)
    when(storageMock.getAttributeStorage).thenReturn(attributeStorage)
    when(storageMock.getWeatherStorage).thenReturn(weatherStorage)
    when(attributeStorage.getAttribute("Zurich", attributeStorage.forecastTsKey)).thenReturn(Future(nowTxt.some))
    val wf = forecastFixture.points.map(w => WeatherForecast("Zurich", now.getMillis, w))
    when(weatherStorage.listRecentForecast("Zurich")).thenReturn(Future(wf))
    val cw = CurrentWeather(
      location = "Zurich", timestamp = now, bootstrapIcon = "icon",
      current = weatherFixture.weather.get.head,
      info = weatherFixture.main.get,
      sunriseSunset = weatherFixture.sys.get
    )
    when(weatherStorage.getRecentWeather("Zurich")).thenReturn(Future(cw.some))

    val controller = new WeatherController(settingsMock, stubControllerComponents()) {
      override def clock(): DateTime = now
    }

    "retrieve forecast" in {
      val result = controller.forecast("Zurich" ).apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val dailyForecast = Helpers.contentAsJson(result).as[List[DailyWeather]]
      dailyForecast must have size 1
      dailyForecast.head.points mustBe forecastFixture.points
    }

    "fail for empty place in weather forecast" in {
      val result = controller.forecast("" ).apply(FakeRequest())
      Helpers.status(result) mustBe Status.BAD_REQUEST
    }

    "retrieve current weather" in {
      val result = controller.current("Zurich" ).apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val currentWeather = Helpers.contentAsJson(result).as[CurrentWeather]
      currentWeather mustBe cw
    }

    "fail for empty place in current weather" in {
      val result = controller.current("" ).apply(FakeRequest())
      Helpers.status(result) mustBe Status.BAD_REQUEST
    }
  }
}
