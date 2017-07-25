package controllers

import org.scalatestplus.play._
import play.api.test._
import akka.util.Timeout
import scala.concurrent.duration._

import scala.language.postfixOps

class AboutControllerSpec extends PlaySpec {

  "controller" should {

    "render about page" in {
      val controller = new AboutController()
      val result = controller.about.apply(FakeRequest())
      implicit val timeout: Timeout = 10 seconds
      val content = Helpers.contentAsString(result)
      content contains "Welcome"
    }
  }
}
