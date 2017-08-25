package controllers

import akka.util.Timeout
import org.scalatestplus.play._
import play.api.test._

import scala.concurrent.duration._
import scala.language.postfixOps

class AboutControllerSpec extends PlaySpec with StubControllerComponentsFactory {

  "controller" should {

    "render about page" in {
      val assetsFinder = new AssetsFinder {
        override def findAssetPath(basePath: String, rawPath: String): String = basePath

        override def assetsUrlPrefix: String = ""

        override def assetsBasePath: String = "public"
      }
      val controller = new AboutController(stubControllerComponents())(assetsFinder)
      val result = controller.about.apply(FakeRequest())
      implicit val timeout: Timeout = 10 seconds
      val content = Helpers.contentAsString(result)
      content contains "Welcome"
    }
  }
}
