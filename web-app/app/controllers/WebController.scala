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
import velocorner.api.Account
import velocorner.build.BuildInfo

import scala.xml.Elem

/**
 * Serves the web pages, rendered from server side. Being replaced with web-front module running on node, with react components.
 */
//noinspection TypeAnnotation
class WebController @Inject() (
    components: ControllerComponents,
    val cache: SyncCacheApi,
    val connectivity: ConnectivitySettings,
    strategy: RefreshStrategy
)(implicit assets: AssetsFinder)
    extends AbstractController(components)
    with AuthChecker {

  def index = AuthAction(parse.default) { implicit request =>
    Ok(views.html.index(getPageContext("Home")))
  }

  def refresh = AuthAsyncAction(parse.default) { implicit request =>
    val maybeAccount = loggedIn
    logger.info(s"refreshing page for $maybeAccount")
    val result = for {
      account <- OptionT[Future, Account](Future(maybeAccount))
      now = DateTime.now()
      // if the access token is expired refresh it and store it
      refreshAccount <- OptionT.liftF(strategy.refreshToken(account, now))
      // retrieve latest activities
      activities <- OptionT.liftF(strategy.refreshAccountActivities(refreshAccount, now))
      _ = logger.info(s"found ${activities.size} new activities")
    } yield ()
    result.value
      .map(_ => Redirect(routes.WebController.index))
      .recover {
        case ex if ex.getMessage.toLowerCase.contains("\"code\":\"invalid\"") =>
          // if the feed fails with expired token, then logout
          logger.info("feed token has been expired, logging out")
          Redirect(auth.routes.StravaController.logout)
      }
  }

  // activity search results
  def search = AuthAction(parse.default) { implicit request =>
    Ok(views.html.search(getPageContext("Search")))
  }

  def best = AuthAction(parse.default) { implicit request =>
    Ok(views.html.best(getPageContext("Best")))
  }

  def marketing = AuthAction(parse.default) { implicit request =>
    Redirect("https://leventes-initial-project-936798.webflow.io/")
  }

  def about = AuthAction(parse.default) { implicit request =>
    Ok(views.html.about(getPageContext("About")))
  }

  def privacy = AuthAction(parse.default) { implicit request =>
    Ok(views.html.privacy(getPageContext("Privacy")))
  }

  def admin = AuthAction(parse.default) { implicit request =>
    Ok(views.html.admin(getPageContext("Admin")))
  }

  def sitemap = Action { _ =>
    val buildTime = java.time.LocalDate.parse(BuildInfo.buildTime, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
    val lastmod = buildTime.format(java.time.format.DateTimeFormatter.ISO_DATE)
    val xml: Elem =
      <urlset
xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9
  http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
  <url>
    <loc>https://velocorner.com/</loc>
    <lastmod>{lastmod}</lastmod>
    <priority>1</priority>
  </url>
  <url>
    <loc>https://velocorner.com/best</loc>
    <lastmod>{lastmod}</lastmod>
    <priority>0.5</priority>
  </url>
  <url>
    <loc>https://velocorner.com/about</loc>
    <lastmod>{lastmod}</lastmod>
    <priority>0.8</priority>
  </url>
  <url>
    <loc>https://velocorner.com/docs</loc>
    <lastmod>{lastmod}</lastmod>
    <priority>0.5</priority>
  </url>
</urlset>
    Ok(xml.toString()).as("application/xml")
  }

  private def getPageContext(title: String)(implicit request: Request[AnyContent]) = {
    val maybeAccount = loggedIn
    val windyEnabled = connectivity.secretConfig.isServiceEnabled(ServiceProvider.Windy)
    val context = PageContext(
      title,
      maybeAccount,
      isWithingsEnabled = connectivity.secretConfig.isServiceEnabled(ServiceProvider.Withings),
      isWindyEnabled = windyEnabled,
      windyApiKey = if (windyEnabled) connectivity.secretConfig.getAuthToken(ServiceProvider.Windy) else "",
      isCrawlerEnabled = connectivity.secretConfig.isServiceEnabled(ServiceProvider.Crawler)
    )
    logger.info(s"rendering ${title.toLowerCase} page for $maybeAccount")
    context
  }
}
