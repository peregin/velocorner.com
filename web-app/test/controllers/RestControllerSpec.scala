package controllers

import org.mockito.ArgumentMatchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import org.mockito.Mockito._
import play.api.http.Status
import velocorner.model.Club
import velocorner.storage.Storage

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RestControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {


  "rest controller for club activity series" should {

    "return with success" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val refreshMock = mock[RefreshStrategy]
      val storageMock = mock[Storage]

      when(settingsMock.getStorage).thenReturn(storageMock)
      when(storageMock.dailyProgressForAll(200)).thenReturn(Seq.empty)
      when(storageMock.getClub(Club.Velocorner)).thenReturn(None)
      when(storageMock.getAthlete(anyLong())).thenReturn(None)
      doNothing().when(refreshMock).refreshClubActivities(anyInt())

      val controller = new RestController(cacheApiMock, settingsMock, refreshMock, stubControllerComponents())
      val result = controller.recentClub("distance").apply(FakeRequest())
      Await.result(result.map(_.header.status), 30 seconds) mustEqual  Status.OK
    }

    "return with not found http status" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val refreshMock = mock[RefreshStrategy]
      val storageMock = mock[Storage]

      when(settingsMock.getStorage).thenReturn(storageMock)
      when(storageMock.dailyProgressForAll(200)).thenReturn(Seq.empty)
      when(storageMock.getClub(Club.Velocorner)).thenReturn(None)
      when(storageMock.getAthlete(anyLong())).thenReturn(None)
      doNothing().when(refreshMock).refreshClubActivities(anyInt())

      val controller = new RestController(cacheApiMock, settingsMock, refreshMock, stubControllerComponents())
      val result = controller.recentClub("blablabla").apply(FakeRequest())
      Await.result(result.map(_.header.status), 30 seconds) mustEqual Status.NOT_FOUND
    }

    "return with internal error http status" in {
      val cacheApiMock = mock[SyncCacheApi]
      val settingsMock = mock[ConnectivitySettings]
      val refreshMock = mock[RefreshStrategy]
      val storageMock = mock[Storage]

      doThrow(new IllegalArgumentException("unexpected server error")).when(refreshMock).refreshClubActivities(anyInt())
      when(settingsMock.getStorage).thenReturn(storageMock)

      val controller = new RestController(cacheApiMock, settingsMock, refreshMock, stubControllerComponents())
      an [IllegalArgumentException] should be thrownBy(controller.recentClub("blablabla").apply(FakeRequest()))
    }
  }
}
