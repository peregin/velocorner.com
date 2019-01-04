package controllers

import org.mockito.ArgumentMatchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import org.mockito.Mockito._
import play.api.http.Status
import velocorner.model.strava.Club
import velocorner.storage.Storage

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ApiControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {


  "rest controller for club activity series" should {

    "return with success" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val storageMock = mock[Storage]

      when(settingsMock.getStorage).thenReturn(storageMock)
      when(storageMock.dailyProgressForAll(200)).thenReturn(Seq.empty)
      when(storageMock.getClub(Club.Velocorner)).thenReturn(None)
      when(storageMock.getAthlete(anyLong())).thenReturn(None)

      val controller = new ApiController(cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.statistics().apply(FakeRequest())
      Await.result(result.map(_.header.status), 30 seconds) mustEqual  Status.OK
    }

    "return with internal error http status" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      doThrow(new IllegalArgumentException("unexpected server error")).when(settingsMock).getStorage

      val controller = new ApiController(cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.yearlyStatistics("distance").apply(FakeRequest())
      Await.result(result.map(_.header.status), 30 seconds) mustEqual Status.INTERNAL_SERVER_ERROR
    }

    "return with forbidden when asking for activities without being logged in" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]

      val controller = new ApiController(cacheApiMock, settingsMock, stubControllerComponents())
      val result = controller.activity(100).apply(FakeRequest())
      Await.result(result.map(_.header.status), 30 seconds) mustEqual Status.FORBIDDEN
    }
  }
}
