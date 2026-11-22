package velocorner.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RouteGeometrySupportSpec extends AnyWordSpec with Matchers {

  "route geometry support" should {
    "parse activity streams with altitude and time" in {
      val streams =
        """{
          |  "latlng": { "type": "latlng", "data": [[47.1, 8.1], [47.2, 8.2]], "series_type": "distance", "original_size": 2, "resolution": "high" },
          |  "time": { "type": "time", "data": [0, 42], "series_type": "distance", "original_size": 2, "resolution": "high" },
          |  "altitude": { "type": "altitude", "data": [510.5, 520.0], "series_type": "distance", "original_size": 2, "resolution": "high" }
          |}""".stripMargin

      val points = RouteGeometrySupport.parseStreams(streams)
      points must have size 2
      points.head mustBe velocorner.api.ActivityRoutePoint(47.1, 8.1, Some(510.5d), Some(0))
      points.last mustBe velocorner.api.ActivityRoutePoint(47.2, 8.2, Some(520d), Some(42))
    }

    "downsample while preserving endpoints" in {
      val points = (0 until 2000).toList.map(i => velocorner.api.ActivityRoutePoint(i.toDouble, i.toDouble, None, Some(i)))
      val normalized = RouteGeometrySupport.normalize(points, limit = 1500)

      normalized must have size 1500
      normalized.head mustBe points.head
      normalized.last mustBe points.last
    }
  }
}
