package controllers

import org.apache.pekko.util.Timeout
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ActivityControllerSpec extends PlaySpec with StubControllerComponentsFactory {

  "rest controller for club activity series" should {

    implicit val timeout: Timeout = new Timeout(10 seconds)
    val refreshStrategyMock = mock(classOf[RefreshStrategy])
    val activityRouteServiceMock = mock(classOf[ActivityRouteService])
    val athletePerformanceServiceMock = mock(classOf[AthletePerformanceService])
    val settingsMock = mock(classOf[ConnectivitySettings])
    val cacheApiMock = mock(classOf[SyncCacheApi])

    "return with success" in {
      val storageMock = mock(classOf[Storage[Future]])

      when(settingsMock.getStorage).thenReturn(storageMock)

      val controller = new ActivityController(
        settingsMock,
        cacheApiMock,
        refreshStrategyMock,
        activityRouteServiceMock,
        athletePerformanceServiceMock,
        stubControllerComponents()
      )
      val result = controller.profile("Ride", "2021").apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
    }

    "return with forbidden when asking for activities without being logged in" in {
      val controller = new ActivityController(
        settingsMock,
        cacheApiMock,
        refreshStrategyMock,
        activityRouteServiceMock,
        athletePerformanceServiceMock,
        stubControllerComponents()
      )
      val result = controller.activity(100).apply(FakeRequest())
      Helpers.status(result) mustBe Status.FORBIDDEN
    }
  }
}
