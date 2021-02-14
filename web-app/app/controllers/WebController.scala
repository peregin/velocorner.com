package controllers

import controllers.auth.{AuthChecker, StravaAuthenticator}
import javax.inject.Inject
import play.api.cache.SyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.implicits._
import cats.data.OptionT
import org.joda.time.DateTime
import velocorner.ServiceProvider

/** Serves the web pages, rendered from server side.
  * Being replaced with web-front module running on node, with react components.
  */
class WebController @Inject() (
    components: ControllerComponents,
    val cache: SyncCacheApi,
    val connectivity: ConnectivitySettings,
    strategy: RefreshStrategy
)(implicit assets: AssetsFinder)
    extends AbstractController(components)
    with AuthChecker {

  def index = AuthAction { implicit request =>
    Ok(views.html.index(getPageContext("Home")))
  }

  def refresh = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    logger.info(s"refreshing page for $maybeAccount")
    val result = for {
      account <- OptionT(Future(maybeAccount))
      now = DateTime.now()
      // if the access token is expired refresh it and store it
      refreshAccount <- OptionT.liftF(
        account.stravaAccess
          .filter(_.accessExpiresAt.isBefore(now))
          .map { stravaAccess =>
            val authenticator = new StravaAuthenticator(connectivity)
            logger.info(s"refreshing access token expired at ${stravaAccess.accessExpiresAt}")
            for {
              resp <- authenticator.refreshAccessToken(stravaAccess.refreshToken)
              refreshAccount = account.copy(stravaAccess = resp.toStravaAccess.some)
              _ <- connectivity.getStorage.getAccountStorage.store(refreshAccount)
            } yield refreshAccount
          }
          .getOrElse(Future(account))
      )
      activities <- OptionT.liftF(strategy.refreshAccountActivities(refreshAccount, now))
      _ = logger.info(s"found ${activities.size} new activities")
    } yield ()
    result.value
      .map(_ => Redirect(routes.WebController.index()))
      .recover {
        case ex if ex.getMessage.toLowerCase.contains("\"code\":\"invalid\"") =>
          // if the feed fails with expired token, then logout
          logger.info("feed token has been expired, logging out")
          Redirect(auth.routes.StravaController.logout())
      }
  }

  def search = AuthAction { implicit request =>
    Ok(views.html.search(getPageContext("Search")))
  }

  def map = AuthAction { implicit request =>
    Ok(views.html.map(getPageContext("Explore")))
  }

  def about = AuthAction { implicit request =>
    Ok(views.html.about(getPageContext("About")))
  }

  def admin = AuthAction { implicit request =>
    Ok(views.html.admin(getPageContext("Admin")))
  }

  private def getPageContext(title: String)(implicit request: Request[AnyContent]) = {
    val maybeAccount = loggedIn
    val windyEnabled = connectivity.secretConfig.isServiceEnabled(ServiceProvider.Windy)
    val context = PageContext(
      title,
      maybeAccount,
      weatherLocation = WeatherCookie.retrieve,
      isWithingsEnabled = connectivity.secretConfig.isServiceEnabled(ServiceProvider.Withings),
      isWindyEnabled = windyEnabled,
      windyApiKey = if (windyEnabled) connectivity.secretConfig.getToken(ServiceProvider.Windy) else ""
    )
    logger.info(s"rendering ${title.toLowerCase} page for $maybeAccount")
    context
  }
}
