package controllers

import javax.inject.Inject
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._

import scala.concurrent.Future

class ErrorController @Inject() (implicit assets: AssetsFinder) extends DefaultHttpErrorHandler {

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    val context = PageContext(
      title = "Not Found",
      account = None,
      weatherLocation = "",
      isWithingsEnabled = false,
      isWindyEnabled = false,
      windyApiKey = "",
      isCrawlerEnabled = false
    )
    Future.successful(NotFound(views.html.notFound(context)(assets = assets)))
  }
}
