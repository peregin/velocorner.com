package controllers

import akka.util.Timeout
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.SyncCacheApi
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import velocorner.SecretConfig

import scala.concurrent.duration._

import scala.language.postfixOps

class ApplicationControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "controller" should {

    implicit val timeout: Timeout = 10 seconds

    val assetsFinder = new AssetsFinder {
      override def findAssetPath(basePath: String, rawPath: String): String = basePath
      override def assetsUrlPrefix: String = ""
      override def assetsBasePath: String = "public"
    }

    val cacheApiMock = mock[SyncCacheApi]
    val settingsMock = mock[ConnectivitySettings]
    val refreshStrategyMock = mock[RefreshStrategy]
    val secretConfigMock = mock[SecretConfig]

    when(settingsMock.secretConfig).thenReturn(secretConfigMock)
    when(secretConfigMock.isWithingsEnabled()).thenReturn(false)

    "render landing page" in {
      val controller = new ApplicationController(
        stubControllerComponents(), cacheApiMock, settingsMock, refreshStrategyMock
      )(assetsFinder)
      val result = controller.index.apply(FakeRequest())
      val content = Helpers.contentAsString(result)
      content must include("Login with your Strava account")
    }

    "render about page" in {
      val controller = new ApplicationController(
        stubControllerComponents(), cacheApiMock, settingsMock, refreshStrategyMock
      )(assetsFinder)
      val result = controller.about.apply(FakeRequest())
      val content = Helpers.contentAsString(result)
      content must include("Welcome to the cycling site")
    }
  }
}
