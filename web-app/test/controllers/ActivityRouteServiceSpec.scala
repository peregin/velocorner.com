package controllers

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, never, verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.api.{Account, ActivityRoute, ActivityRoutePoint, OAuth2Access, Units}
import velocorner.api.strava.Activity
import velocorner.model.strava.Athlete
import velocorner.storage.Storage

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ActivityRouteServiceSpec extends AnyWordSpec with Matchers {

  private def await[T](future: Future[T]): T = scala.concurrent.Await.result(future, 5.seconds)

  private def account = Account(
    athleteId = 432909L,
    displayName = "Levi",
    displayLocation = "Zurich, Switzerland",
    avatarUrl = "",
    lastUpdate = None,
    role = None,
    unit = Some(Units.Metric),
    stravaAccess = Some(OAuth2Access("token", DateTime.now().plusHours(1), "refresh"))
  )

  private def activity = Activity(
    id = 244993130L,
    resource_state = 2,
    external_id = None,
    upload_id = None,
    athlete = Athlete(432909L, 1, Some("Levi"), None, None, None, None, None, None),
    name = "Test Ride",
    distance = 1000f,
    moving_time = 300,
    elapsed_time = 320,
    total_elevation_gain = 20f,
    `type` = "Ride",
    start_date = DateTime.now(),
    start_date_local = Some(DateTime.now()),
    average_speed = None,
    max_speed = None,
    average_cadence = None,
    average_temp = None,
    average_watts = None,
    max_watts = None,
    average_heartrate = None,
    max_heartrate = None,
    gear_id = None,
    start_latitude = Some(47.3f),
    start_longitude = Some(8.5f),
    commute = None,
    elev_high = None,
    elev_low = None,
    pr_count = None
  )

  "activity route service" should {
    "return cached route without calling Strava" in {
      val connectivity = mock(classOf[ConnectivitySettings])
      val strategy = mock(classOf[RefreshStrategy])
      val storage = mock(classOf[Storage[Future]])
      val cached = ActivityRoute(
        activityId = 244993130L,
        source = "streams",
        points = List(
          ActivityRoutePoint(47.3, 8.5, Some(500d), Some(0)),
          ActivityRoutePoint(47.4, 8.6, Some(510d), Some(42))
        )
      )

      when(connectivity.getStorage).thenReturn(storage)
      when(storage.getActivityRoute(244993130L, 432909L)).thenReturn(Future.successful(Some(cached)))

      val service = new ActivityRouteService(connectivity, strategy)
      await(service.routeFor(account, 244993130L)).map(_.source) mustBe Some("streams")

      verify(strategy, never()).refreshToken(any[Account], any[DateTime])
    }

    "fetch activity streams route and store parsed cache data" in {
      val connectivity = mock(classOf[ConnectivitySettings])
      val strategy = mock(classOf[RefreshStrategy])
      val storage = mock(classOf[Storage[Future]])
      val feed = mock(classOf[velocorner.feed.StravaActivityFeed])
      val rawStreams =
        """{
          |  "latlng": { "type": "latlng", "data": [[47.1, 8.1], [47.2, 8.2]], "series_type": "distance", "original_size": 2, "resolution": "high" },
          |  "time": { "type": "time", "data": [0, 30], "series_type": "distance", "original_size": 2, "resolution": "high" },
          |  "altitude": { "type": "altitude", "data": [500.0, 510.0], "series_type": "distance", "original_size": 2, "resolution": "high" }
          |}""".stripMargin

      when(connectivity.getStorage).thenReturn(storage)
      when(storage.getActivityRoute(244993130L, 432909L)).thenReturn(Future.successful(None))
      when(storage.getActivity(244993130L)).thenReturn(Future.successful(Some(activity)))
      when(strategy.refreshToken(any[Account], any[DateTime])).thenReturn(Future.successful(account))
      when(connectivity.getStravaFeed("token")).thenReturn(feed)
      when(feed.fetchRouteStreams(244993130L)).thenReturn(Future.successful(rawStreams))
      when(storage.storeActivityRoute(any[ActivityRoute], eqTo(432909L))).thenReturn(Future.successful(()))

      val service = new ActivityRouteService(connectivity, strategy)
      val route = await(service.routeFor(account, 244993130L)).get

      route.source mustBe "streams"
      route.points must have size 2
      route.points.head.ts mustBe Some(0)
      verify(storage).storeActivityRoute(
        eqTo(route),
        eqTo(432909L)
      )
    }

    "return none when activity belongs to another athlete" in {
      val connectivity = mock(classOf[ConnectivitySettings])
      val strategy = mock(classOf[RefreshStrategy])
      val storage = mock(classOf[Storage[Future]])
      val foreignActivity = activity.copy(athlete = Athlete(1L, 1, Some("Other"), None, None, None, None, None, None))

      when(connectivity.getStorage).thenReturn(storage)
      when(storage.getActivityRoute(244993130L, 432909L)).thenReturn(Future.successful(None))
      when(storage.getActivity(244993130L)).thenReturn(Future.successful(Some(foreignActivity)))

      val service = new ActivityRouteService(connectivity, strategy)
      await(service.routeFor(account, 244993130L)) mustBe None
    }
  }
}
