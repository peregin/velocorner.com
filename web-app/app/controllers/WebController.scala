package controllers

import controllers.auth.AuthChecker
import javax.inject.Inject
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.mvc._
import scalaz.Scalaz._
import scalaz._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Serves the web pages, rendered from server side.
  * Being replaced with web-front module running on node, with react components.
  */
class WebController @Inject()
  (components: ControllerComponents,
   val cache: SyncCacheApi, val connectivity: ConnectivitySettings,
   strategy: RefreshStrategy)
  (implicit assets: AssetsFinder) extends AbstractController(components) with AuthChecker {

  private val logger = Logger.of(this.getClass)

  def index = AuthAction { implicit request =>
    Ok(views.html.index(getPageContext("Home")))
  }

  def refresh = AuthAsyncAction { implicit request =>
    val maybeAccount = loggedIn
    logger.info(s"refreshing page for $maybeAccount")
    val result = for {
      account <- OptionT(Future(maybeAccount))
      activities <- strategy.refreshAccountActivities(account).liftM[OptionT]
      _ = logger.info(s"found ${activities.size} new activities")
    } yield ()
    result.run
      .map(_ => Redirect(routes.WebController.index()))
      .recover{ case ex if ex.getMessage.toLowerCase.contains("\"code\":\"invalid\"") =>
        // if the feed fails with expired token, then logout
        logger.info("feed token has been expired, logging out")
        Redirect(auth.routes.StravaController.logout())
      }
  }

  def search = AuthAction { implicit request =>
    Ok(views.html.search(getPageContext("Search")))
  }

  def about = AuthAction { implicit request =>
    Ok(views.html.about(getPageContext("About")))
  }

  private def getPageContext(title: String)(implicit request: Request[AnyContent]) = {
    val maybeAccount = loggedIn
    val context = PageContext(title, maybeAccount,
      connectivity.secretConfig.isWithingsEnabled(),
      connectivity.secretConfig.isWeatherEnabled(), WeatherCookie.retrieve
    )
    logger.info(s"rendering ${title.toLowerCase} page for $maybeAccount")
    context
  }
}