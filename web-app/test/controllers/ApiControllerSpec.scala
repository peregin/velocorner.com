package controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Environment
import play.api.cache.SyncCacheApi
import play.api.http.Status
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import velocorner.manual.AwaitSupport
import velocorner.model.strava.Club
import velocorner.storage.Storage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar with AwaitSupport {

  "rest controller for club activity series" should {

    "return with success" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val storageMock = mock[Storage]

      when(settingsMock.getStorage).thenReturn(storageMock)
      when(storageMock.getClub(Club.Velocorner)).thenReturn(Future(None))
      when(storageMock.getAthlete(anyLong())).thenReturn(Future(None))

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.statistics().apply(FakeRequest())
      await(result.map(_.header.status)) mustEqual  Status.OK
    }

    "return with forbidden when asking for activities without being logged in" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.activity(100).apply(FakeRequest())
      await(result.map(_.header.status)) mustEqual Status.FORBIDDEN
    }

    "return system status" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      val controller = new ApiController(Environment.simple(), cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.status().apply(FakeRequest())
      val response = await(result)
      response.header.status mustEqual Status.OK
    }
  }
}
