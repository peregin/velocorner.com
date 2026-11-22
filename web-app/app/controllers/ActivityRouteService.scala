package controllers

import cats.data.OptionT
import org.joda.time.DateTime
import velocorner.api.{Account, ActivityRoute}
import velocorner.util.RouteGeometrySupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.Logger

@Singleton
class ActivityRouteService @Inject() (connectivity: ConnectivitySettings, strategy: RefreshStrategy) {

  private val logger = Logger.of(this.getClass)

  def routeFor(account: Account, activityId: Long): Future[Option[ActivityRoute]] = {
    val storage = connectivity.getStorage

    val result = for {
      cached <- OptionT(storage.getActivityRoute(activityId, account.athleteId))
    } yield cached

    result.value.flatMap {
      case some @ Some(_) => Future.successful(some)
      case None           => fetchAndCacheRoute(account, activityId)
    }
  }

  private def fetchAndCacheRoute(account: Account, activityId: Long): Future[Option[ActivityRoute]] = {
    val storage = connectivity.getStorage

    val result = for {
      activity <- OptionT(storage.getActivity(activityId)).filter(_.athlete.id == account.athleteId)
      refreshedAccount <- OptionT.liftF(strategy.refreshToken(account, DateTime.now()))
      accessToken <- OptionT.fromOption[Future](refreshedAccount.stravaAccess.map(_.accessToken))
      route <- OptionT(fetchFromStrava(accessToken, activityId))
      _ <- OptionT.liftF(storage.storeActivityRoute(route, account.athleteId))
    } yield route

    result.value
  }

  private def fetchFromStrava(accessToken: String, activityId: Long): Future[Option[ActivityRoute]] = {
    val feed = connectivity.getStravaFeed(accessToken)

    val result = feed
      .fetchRouteStreams(activityId)
      .map { rawStreams =>
        RouteGeometrySupport.fromStreams(activityId, rawStreams)
      }
      .map { maybeRoute =>
        maybeRoute.foreach(route => logger.debug(s"streams route points for activity $activityId: ${route.points.size}"))
        maybeRoute
      }
      .recover { case error =>
        logger.info(s"Route geometry retrieval failed for activity $activityId: ${error.getMessage}")
        None
      }

    result.andThen { case _ => feed.close() }
  }
}
