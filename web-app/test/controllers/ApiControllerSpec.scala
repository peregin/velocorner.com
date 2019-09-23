package controllers

import akka.util.Timeout
import model.StatusInfo
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.{Environment, Mode}
import play.api.cache.SyncCacheApi
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.model.strava.Club
import velocorner.storage.Storage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ApiControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for club activity series" should {

    implicit val timeout = new Timeout(10 seconds)

    "return with success" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val storageMock = mock[Storage]

      when(settingsMock.getStorage).thenReturn(storageMock)
      when(storageMock.getClub(Club.Velocorner)).thenReturn(Future(None))
      when(storageMock.getAthlete(anyLong())).thenReturn(Future(None))

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.ytdProgress.apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
    }

    "return with forbidden when asking for activities without being logged in" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.activity(100).apply(FakeRequest())
      Helpers.status(result) mustBe Status.FORBIDDEN
    }

    "return system status" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.status().apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val statusInfo = Helpers.contentAsJson(result).as[StatusInfo]
      statusInfo.applicationMode mustBe Mode.Test
    }
  }
}
