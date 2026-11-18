package controllers

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Environment
import play.api.inject.DefaultApplicationLifecycle
import play.api.test.StubControllerComponentsFactory
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import velocorner.SecretConfig

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class ResourceLifecycleSpec extends AnyWordSpec with Matchers with StubControllerComponentsFactory {

  "application shutdown" should {
    "close all product HTTP clients without stopping Play's runtime" in {
      val system = ActorSystem("product-lifecycle")
      val lifecycle = new DefaultApplicationLifecycle()
      try {
        val connectivity = mock(classOf[ConnectivitySettings])
        when(connectivity.secretConfig).thenReturn(SecretConfig(ConfigFactory.empty()))
        when(connectivity.materializer).thenReturn(Materializer(system))
        val controller = new ProductsController(connectivity, Environment.simple(), stubControllerComponents(), lifecycle)
        val clients = List(controller.productFeed, controller.ratesFeed, controller.weatherFeed).map(_.ws(_.underlying[AsyncHttpClient]))
        clients.foreach(_.isClosed mustBe false)

        Await.result(lifecycle.stop(), 10.seconds)

        clients.foreach(_.isClosed mustBe true)
        system.whenTerminated.isCompleted mustBe false
      } finally {
        Await.result(lifecycle.stop(), 10.seconds)
        Await.result(system.terminate(), 10.seconds)
      }
    }

    "terminate the AI HTTP client" in {
      val lifecycle = new DefaultApplicationLifecycle()
      val service = new AthletePerformanceService(mock(classOf[ConnectivitySettings]), lifecycle)(using ExecutionContext.global)
      try {
        Await.result(lifecycle.stop(), 10.seconds)
        service.httpClient.awaitTermination(java.time.Duration.ofSeconds(5)) mustBe true
        service.httpClient.isTerminated mustBe true
      } finally Await.result(lifecycle.stop(), 10.seconds)
    }
  }
}
