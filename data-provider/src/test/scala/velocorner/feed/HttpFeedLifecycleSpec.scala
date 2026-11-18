package velocorner.feed

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient
import velocorner.SecretConfig

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpFeedLifecycleSpec extends AnyWordSpec with Matchers {

  "HTTP feeds" should {
    "close their client without stopping a borrowed runtime, including after a restart" in {
      for (_ <- 1 to 2) {
        val system = ActorSystem("http-feed-lifecycle")
        try {
          val feed = new ProductCrawlerFeed(SecretConfig(ConfigFactory.empty()), Materializer(system))
          val client = feed.ws(_.underlying[AsyncHttpClient])
          try client.isClosed mustBe false
          finally feed.close()

          client.isClosed mustBe true
          system.whenTerminated.isCompleted mustBe false
        } finally Await.result(system.terminate(), 10.seconds)
      }
    }
  }
}
