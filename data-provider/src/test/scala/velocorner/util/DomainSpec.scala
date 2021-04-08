package velocorner.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.net.URI

/**
  * Created by levi on 25/01/16.
  *  e.g.:
  * http://localhost:9000/authorize/strava
  * http://velocorner.com/authorize/strava
  */
class DomainSpec extends AnyWordSpec with Matchers {

  "uri parser" should {
    "extract host name for localhost with port number" in {
      val uri = new URI("http://localhost:9000/authorize/strava")
      uri.getHost === "localhost"
      uri.getPort === 9000
      uri.getPath === "/authorize/strava"
      uri.getScheme === "http"
    }

    "parse callback url" in {
      val uri = new URI("http://velocorner.com/authorize/strava")
      uri.getHost === "velocorner.com"
      uri.getPort === -1
      uri.getPath === "/authorize/strava"
    }
  }
}
