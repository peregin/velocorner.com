package controllers

import org.apache.pekko.util.Timeout
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ActivityControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for club activity series" should {

    implicit val timeout: Timeout = new Timeout(10 seconds)
    val refreshStrategyMock = mock[RefreshStrategy]
    val settingsMock = mock[ConnectivitySettings]
    val cacheApiMock = mock[SyncCacheApi]

    "return with success" in {
      val storageMock = mock[Storage[Future]]

      when(settingsMock.getStorage).thenReturn(storageMock)

      val controller = new ActivityController(settingsMock, cacheApiMock, refreshStrategyMock, stubControllerComponents())
      val result = controller.profile("Ride", "2021").apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
    }

    "return with forbidden when asking for activities without being logged in" in {
      val controller = new ActivityController(settingsMock, cacheApiMock, refreshStrategyMock, stubControllerComponents())
      val result = controller.activity(100).apply(FakeRequest())
      Helpers.status(result) mustBe Status.FORBIDDEN
    }
  }
}
