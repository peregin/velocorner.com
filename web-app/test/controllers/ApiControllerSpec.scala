package controllers

import akka.util.Timeout
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import play.api.{Environment, Mode}
import velocorner.api.StatusInfo
import velocorner.search.BrandSearch

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ApiControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for club activity series" should {

    implicit val timeout = new Timeout(10 seconds)

    "return system status" in {
      val settingsMock = mock[ConnectivitySettings]
      val brandFeedMock = mock[BrandSearch]
      when(brandFeedMock.version()).thenReturn(Future.successful("1.2.3"))

      val controller = new ApiController(Environment.simple(), settingsMock, stubControllerComponents()) {
        override lazy val brandFeed: BrandSearch = brandFeedMock
      }
      val result = controller.status().apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val statusInfo = Helpers.contentAsJson(result).as[StatusInfo]
      statusInfo.applicationMode mustBe Mode.Test.toString
      statusInfo.zincVersion mustBe "1.2.3"
    }

    "return partial system status, when some queries failing" in {
      val settingsMock = mock[ConnectivitySettings]
      val brandFeedMock = mock[BrandSearch]
      when(brandFeedMock.version()).thenReturn(Future.failed(new IllegalArgumentException("failed to connect")))

      val controller = new ApiController(Environment.simple(), settingsMock, stubControllerComponents()) {
        override lazy val brandFeed: BrandSearch = brandFeedMock
      }
      val result = controller.status().apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val statusInfo = Helpers.contentAsJson(result).as[StatusInfo]
      statusInfo.zincVersion mustBe "n/a"
    }
  }
}
