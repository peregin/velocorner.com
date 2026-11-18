package controllers

import controllers.auth.JwtUser
import org.apache.pekko.util.Timeout
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.SecretConfig
import velocorner.api.strava.Activity
import velocorner.model.AccountFixtures
import velocorner.storage.{AccountStorage, Storage}
import velocorner.util.JsonIo

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ActivityControllerSpec extends PlaySpec with StubControllerComponentsFactory with AccountFixtures {

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

    val activity = JsonIo.readReadFromResource[Activity]("/data/strava/activity-hr.json")
    val owner = account.copy(athleteId = activity.athlete.id)

    Seq(
      ("return the owner's activity", owner, Some(activity), Status.OK),
      ("hide another athlete's activity", owner.copy(athleteId = owner.athleteId + 1), Some(activity), Status.NOT_FOUND),
      ("return not found for a missing activity", owner, None, Status.NOT_FOUND)
    ).foreach { case (description, authenticatedAccount, storedActivity, expectedStatus) =>
      description in {
        val storageMock = mock(classOf[Storage[Future]])
        val accountStorageMock = mock(classOf[AccountStorage[Future]])
        val secretConfigMock = mock(classOf[SecretConfig])

        when(settingsMock.getStorage).thenReturn(storageMock)
        when(settingsMock.secretConfig).thenReturn(secretConfigMock)
        when(secretConfigMock.getJwtSecret).thenReturn("test-secret")
        when(storageMock.getAccountStorage).thenReturn(accountStorageMock)
        when(accountStorageMock.getAccount(authenticatedAccount.athleteId)).thenReturn(Future.successful(Some(authenticatedAccount)))
        when(storageMock.getActivity(activity.id)).thenReturn(Future.successful(storedActivity))

        val controller = new ActivityController(
          settingsMock,
          cacheApiMock,
          refreshStrategyMock,
          activityRouteServiceMock,
          athletePerformanceServiceMock,
          stubControllerComponents()
        )
        val token = JwtUser.toJwtUser(authenticatedAccount).toToken(using "test-secret")
        val request = FakeRequest().withHeaders("Authorization" -> s"Bearer $token")
        val result = controller.activity(activity.id).apply(request)

        Helpers.status(result) mustBe expectedStatus
        if (expectedStatus == Status.OK) Helpers.contentAsJson(result).as[Activity] mustBe activity
        else Helpers.contentAsString(result) mustBe empty
      }
    }
  }
}
